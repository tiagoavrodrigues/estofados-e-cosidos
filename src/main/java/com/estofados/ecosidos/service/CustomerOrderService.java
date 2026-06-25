package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.Customer;
import com.estofados.ecosidos.domain.CustomerOrder;
import com.estofados.ecosidos.domain.CustomerOrderLine;
import com.estofados.ecosidos.domain.CustomerOrderStatus;
import com.estofados.ecosidos.domain.CustomerOrderStatusCode;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.domain.PartRawMaterialRequirement;
import com.estofados.ecosidos.domain.RawMaterial;
import com.estofados.ecosidos.domain.Stock;
import com.estofados.ecosidos.domain.StockReservation;
import com.estofados.ecosidos.domain.StockReservationStatusCode;
import com.estofados.ecosidos.domain.StockStatusCode;
import com.estofados.ecosidos.repository.BillOfMaterialItemRepository;
import com.estofados.ecosidos.repository.CustomerOrderLineRepository;
import com.estofados.ecosidos.repository.CustomerOrderRepository;
import com.estofados.ecosidos.repository.CustomerOrderStatusRepository;
import com.estofados.ecosidos.repository.PartRawMaterialRequirementRepository;
import com.estofados.ecosidos.repository.CustomerRepository;
import com.estofados.ecosidos.repository.PartRepository;
import com.estofados.ecosidos.repository.StockRepository;
import com.estofados.ecosidos.repository.StockReservationRepository;
import com.estofados.ecosidos.service.input.CustomerOrderCreateInput;
import com.estofados.ecosidos.service.input.CustomerOrderLineCreateInput;
import com.estofados.ecosidos.service.result.CustomerOrderCreateResult;
import com.estofados.ecosidos.service.result.CustomerOrderDetailResult;
import com.estofados.ecosidos.service.result.CustomerOrderLineResult;
import com.estofados.ecosidos.service.result.CustomerOrderMaterialAvailabilityResult;
import com.estofados.ecosidos.service.result.CustomerOrderMaterialRequirementResult;
import com.estofados.ecosidos.service.result.CustomerOrderSummaryResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private static final String FINISHED_PRODUCT_PART_TYPE_CODE = "FINISHED_PRODUCT";
    private static final List<String> ACTIVE_STOCK_RESERVATION_STATUS_CODES = List.of(
            StockReservationStatusCode.RESERVED.name(),
            StockReservationStatusCode.PARTIALLY_CONSUMED.name());
    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int CALCULATION_SCALE = 6;
    private static final RoundingMode CALCULATION_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final CustomerRepository customerRepository;
    private final CustomerOrderStatusRepository customerOrderStatusRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerOrderLineRepository customerOrderLineRepository;
    private final PartRepository partRepository;
    private final BillOfMaterialItemRepository billOfMaterialItemRepository;
    private final PartRawMaterialRequirementRepository partRawMaterialRequirementRepository;
    private final StockRepository stockRepository;
    private final StockReservationRepository stockReservationRepository;

    @Transactional
    public CustomerOrderCreateResult create(CustomerOrderCreateInput input) {
        validateInput(input);

        Customer customer = findCustomerById(input.customerId());
        CustomerOrderStatus receivedStatus = findStatus(CustomerOrderStatusCode.RECEIVED);

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setCode(generateCode());
        customerOrder.setCustomer(customer);
        customerOrder.setStatus(receivedStatus);
        customerOrder.setOrderDate(input.orderDate() != null ? input.orderDate() : LocalDate.now());
        customerOrder.setNotes(input.notes());

        CustomerOrder savedCustomerOrder = customerOrderRepository.save(customerOrder);

        for (CustomerOrderLineCreateInput lineInput : input.lines()) {
            CustomerOrderLine line = createLine(savedCustomerOrder, lineInput);
            customerOrderLineRepository.save(line);
        }

        return new CustomerOrderCreateResult(
                savedCustomerOrder.getId(),
                savedCustomerOrder.getCode(),
                customer.getId(),
                receivedStatus.getCode(),
                savedCustomerOrder.getOrderDate(),
                savedCustomerOrder.getNotes());
    }

    @Transactional(readOnly = true)
    public CustomerOrderDetailResult findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        CustomerOrder customerOrder = findCustomerOrderById(id);

        return toDetailResult(customerOrder);
    }

    @Transactional
    public CustomerOrderDetailResult validate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        CustomerOrder customerOrder = findCustomerOrderById(id);

        validateCanValidate(customerOrder);

        CustomerOrderStatus validatedStatus = findStatus(CustomerOrderStatusCode.VALIDATED);
        customerOrder.setStatus(validatedStatus);
        CustomerOrder savedCustomerOrder = customerOrderRepository.save(customerOrder);

        return toDetailResult(savedCustomerOrder);
    }

    @Transactional
    public CustomerOrderDetailResult cancel(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        CustomerOrder customerOrder = findCustomerOrderById(id);

        validateCanCancel(customerOrder);

        CustomerOrderStatus cancelledStatus = findStatus(CustomerOrderStatusCode.CANCELLED);
        customerOrder.setStatus(cancelledStatus);
        CustomerOrder savedCustomerOrder = customerOrderRepository.save(customerOrder);

        return toDetailResult(savedCustomerOrder);
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrderSummaryResult> findAll(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable is required.");
        }

        return customerOrderRepository.findAllByOrderByIdDesc(pageable)
                .map(this::toSummaryResult);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderMaterialRequirementResult> findMaterialRequirements(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        CustomerOrder customerOrder = findCustomerOrderById(id);
        Map<MaterialRequirementKey, MaterialRequirementAccumulator> requirements = new HashMap<>();

        for (CustomerOrderLine line : customerOrderLineRepository.findByCustomerOrderId(customerOrder.getId())) {
            addLineMaterialRequirements(line, requirements);
        }

        return requirements.values().stream()
                .map(MaterialRequirementAccumulator::toResult)
                .sorted(Comparator.comparing(CustomerOrderMaterialRequirementResult::rawMaterialCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderMaterialAvailabilityResult> findMaterialAvailability(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        return findMaterialRequirements(id).stream()
                .map(this::toMaterialAvailabilityResult)
                .toList();
    }

    private CustomerOrderDetailResult toDetailResult(CustomerOrder customerOrder) {
        Customer customer = customerOrder.getCustomer();
        CustomerOrderStatus status = customerOrder.getStatus();
        List<CustomerOrderLineResult> lines = customerOrderLineRepository.findByCustomerOrderId(customerOrder.getId()).stream()
                .map(this::toLineResult)
                .toList();

        return new CustomerOrderDetailResult(
                customerOrder.getId(),
                customerOrder.getCode(),
                customer.getId(),
                customer.getCode(),
                customer.getName(),
                status.getCode(),
                customerOrder.getOrderDate(),
                customerOrder.getNotes(),
                lines);
    }

    private void validateInput(CustomerOrderCreateInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Customer order input is required.");
        }

        if (input.customerId() == null) {
            throw new IllegalArgumentException("Customer id is required.");
        }

        if (input.lines() == null || input.lines().isEmpty()) {
            throw new IllegalArgumentException("Customer order must have at least one line.");
        }

        for (CustomerOrderLineCreateInput line : input.lines()) {
            validateLineInput(line);
        }
    }

    private void validateLineInput(CustomerOrderLineCreateInput line) {
        if (line == null) {
            throw new IllegalArgumentException("Customer order line is required.");
        }

        if (line.partId() == null) {
            throw new IllegalArgumentException("Part id is required.");
        }

        if (line.quantity() == null || line.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Line quantity must be greater than zero.");
        }
    }

    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }

    private CustomerOrderStatus findStatus(CustomerOrderStatusCode statusCode) {
        return customerOrderStatusRepository.findByCode(statusCode.name())
                .orElseThrow(() -> new IllegalStateException("Customer order status not found: "
                        + statusCode.name()));
    }

    private CustomerOrder findCustomerOrderById(Long id) {
        return customerOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer order not found: " + id));
    }

    private Part findPartById(Long partId) {
        return partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found: " + partId));
    }

    private void validateCustomerOrderPart(Part part) {
        if (part.getPartType() == null
                || !FINISHED_PRODUCT_PART_TYPE_CODE.equals(part.getPartType().getCode())) {
            throw new IllegalArgumentException("Customer order line part must be a finished product: "
                    + part.getId());
        }
    }

    private void validateCanCancel(CustomerOrder customerOrder) {
        if (hasStatus(customerOrder, CustomerOrderStatusCode.CANCELLED)) {
            throw new IllegalArgumentException("Customer order is already cancelled: " + customerOrder.getId());
        }
    }

    private void validateCanValidate(CustomerOrder customerOrder) {
        String currentStatusCode = getStatusCode(customerOrder);

        if (CustomerOrderStatusCode.RECEIVED.name().equals(currentStatusCode)) {
            return;
        }

        if (CustomerOrderStatusCode.CANCELLED.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Cancelled customer order cannot be validated: "
                    + customerOrder.getId());
        }

        if (CustomerOrderStatusCode.VALIDATED.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Customer order is already validated: " + customerOrder.getId());
        }

        throw new IllegalArgumentException("Customer order cannot be validated from status: " + currentStatusCode);
    }

    private boolean hasStatus(CustomerOrder customerOrder, CustomerOrderStatusCode statusCode) {
        if (customerOrder.getStatus() == null) {
            return false;
        }

        return statusCode.name().equals(customerOrder.getStatus().getCode());
    }

    private String getStatusCode(CustomerOrder customerOrder) {
        if (customerOrder.getStatus() == null) {
            return null;
        }

        return customerOrder.getStatus().getCode();
    }

    private String generateCode() {
        return "CO-" + CODE_FORMATTER.format(LocalDateTime.now());
    }

    private CustomerOrderLine createLine(CustomerOrder customerOrder, CustomerOrderLineCreateInput input) {
        Part part = findPartById(input.partId());
        validateCustomerOrderPart(part);

        CustomerOrderLine line = new CustomerOrderLine();
        line.setCustomerOrder(customerOrder);
        line.setPart(part);
        line.setQuantity(input.quantity());

        return line;
    }

    private void addLineMaterialRequirements(
            CustomerOrderLine line,
            Map<MaterialRequirementKey, MaterialRequirementAccumulator> requirements) {
        for (var billOfMaterialItem : billOfMaterialItemRepository.findByParentPartIdAndActiveTrue(line.getPart().getId())) {
            BigDecimal componentQuantity = line.getQuantity().multiply(billOfMaterialItem.getQuantity());

            for (PartRawMaterialRequirement requirement
                    : partRawMaterialRequirementRepository.findByPartIdAndActiveTrue(
                            billOfMaterialItem.getComponentPart().getId())) {
                BigDecimal requiredQuantity = componentQuantity
                        .multiply(requirement.getQuantity())
                        .multiply(wasteFactor(requirement.getWastePercentage()));
                RawMaterial rawMaterial = requirement.getRawMaterial();
                MaterialRequirementKey key = new MaterialRequirementKey(rawMaterial.getId(), requirement.getUnit());

                requirements.computeIfAbsent(
                                key,
                                ignored -> new MaterialRequirementAccumulator(
                                        rawMaterial.getId(),
                                        rawMaterial.getCode(),
                                        rawMaterial.getName(),
                                        requirement.getUnit()))
                        .add(requiredQuantity);
            }
        }
    }

    private BigDecimal wasteFactor(BigDecimal wastePercentage) {
        if (wastePercentage == null) {
            return BigDecimal.ONE;
        }

        return BigDecimal.ONE.add(
                wastePercentage.divide(ONE_HUNDRED, CALCULATION_SCALE, CALCULATION_ROUNDING_MODE));
    }

    private CustomerOrderMaterialAvailabilityResult toMaterialAvailabilityResult(
            CustomerOrderMaterialRequirementResult requirement) {
        BigDecimal availableQuantity = calculateAvailableQuantity(requirement.rawMaterialId(), requirement.unit());
        BigDecimal missingQuantity = max(requirement.requiredQuantity().subtract(availableQuantity), BigDecimal.ZERO);

        return new CustomerOrderMaterialAvailabilityResult(
                requirement.rawMaterialId(),
                requirement.rawMaterialCode(),
                requirement.rawMaterialName(),
                requirement.requiredQuantity(),
                availableQuantity,
                missingQuantity,
                requirement.unit(),
                missingQuantity.compareTo(BigDecimal.ZERO) == 0);
    }

    private BigDecimal calculateAvailableQuantity(Long rawMaterialId, String unit) {
        BigDecimal stockQuantity = stockRepository
                .findByRawMaterialIdAndUnitAndStockStatusCode(rawMaterialId, unit, StockStatusCode.AVAILABLE.name()).stream()
                .map(Stock::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal reservedQuantity = stockReservationRepository
                .findByRawMaterialIdAndUnitAndStatusCodeIn(
                        rawMaterialId,
                        unit,
                        ACTIVE_STOCK_RESERVATION_STATUS_CODES)
                .stream()
                .map(this::remainingReservedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return max(stockQuantity.subtract(reservedQuantity), BigDecimal.ZERO);
    }

    private BigDecimal remainingReservedQuantity(StockReservation reservation) {
        BigDecimal reservedQuantity = reservation.getReservedQuantity() != null
                ? reservation.getReservedQuantity()
                : BigDecimal.ZERO;
        BigDecimal consumedQuantity = reservation.getConsumedQuantity() != null
                ? reservation.getConsumedQuantity()
                : BigDecimal.ZERO;

        return max(reservedQuantity.subtract(consumedQuantity), BigDecimal.ZERO);
    }

    private BigDecimal max(BigDecimal first, BigDecimal second) {
        if (first.compareTo(second) >= 0) {
            return first;
        }

        return second;
    }

    private CustomerOrderLineResult toLineResult(CustomerOrderLine line) {
        Part part = line.getPart();

        return new CustomerOrderLineResult(
                line.getId(),
                part.getId(),
                part.getCode(),
                part.getName(),
                line.getQuantity());
    }

    private CustomerOrderSummaryResult toSummaryResult(CustomerOrder customerOrder) {
        Customer customer = customerOrder.getCustomer();
        CustomerOrderStatus status = customerOrder.getStatus();

        return new CustomerOrderSummaryResult(
                customerOrder.getId(),
                customerOrder.getCode(),
                customer.getId(),
                customer.getCode(),
                customer.getName(),
                status.getCode(),
                customerOrder.getOrderDate());
    }

    private record MaterialRequirementKey(Long rawMaterialId, String unit) {
    }

    private static class MaterialRequirementAccumulator {

        private final Long rawMaterialId;
        private final String rawMaterialCode;
        private final String rawMaterialName;
        private final String unit;
        private BigDecimal requiredQuantity = BigDecimal.ZERO;

        private MaterialRequirementAccumulator(
                Long rawMaterialId,
                String rawMaterialCode,
                String rawMaterialName,
                String unit) {
            this.rawMaterialId = rawMaterialId;
            this.rawMaterialCode = rawMaterialCode;
            this.rawMaterialName = rawMaterialName;
            this.unit = unit;
        }

        private void add(BigDecimal quantity) {
            requiredQuantity = requiredQuantity.add(quantity);
        }

        private CustomerOrderMaterialRequirementResult toResult() {
            return new CustomerOrderMaterialRequirementResult(
                    rawMaterialId,
                    rawMaterialCode,
                    rawMaterialName,
                    requiredQuantity,
                    unit);
        }
    }
}
