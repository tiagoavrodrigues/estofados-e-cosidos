package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.Customer;
import com.estofados.ecosidos.domain.CustomerOrder;
import com.estofados.ecosidos.domain.CustomerOrderLine;
import com.estofados.ecosidos.domain.CustomerOrderStatus;
import com.estofados.ecosidos.domain.CustomerOrderStatusCode;
import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.ManufacturingOrderStatus;
import com.estofados.ecosidos.domain.ManufacturingOrderStatusCode;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.domain.RawMaterial;
import com.estofados.ecosidos.domain.StockReservation;
import com.estofados.ecosidos.domain.StockReservationStatus;
import com.estofados.ecosidos.domain.StockReservationStatusCode;
import com.estofados.ecosidos.repository.CustomerOrderLineRepository;
import com.estofados.ecosidos.repository.CustomerOrderRepository;
import com.estofados.ecosidos.repository.CustomerOrderStatusRepository;
import com.estofados.ecosidos.repository.CustomerRepository;
import com.estofados.ecosidos.repository.ManufacturingOrderRepository;
import com.estofados.ecosidos.repository.ManufacturingOrderStatusRepository;
import com.estofados.ecosidos.repository.PartRepository;
import com.estofados.ecosidos.repository.RawMaterialRepository;
import com.estofados.ecosidos.repository.StockReservationRepository;
import com.estofados.ecosidos.repository.StockReservationStatusRepository;
import com.estofados.ecosidos.service.input.CustomerOrderCreateInput;
import com.estofados.ecosidos.service.input.CustomerOrderLineCreateInput;
import com.estofados.ecosidos.service.result.CustomerOrderCreateResult;
import com.estofados.ecosidos.service.result.CustomerOrderDetailResult;
import com.estofados.ecosidos.service.result.CustomerOrderLineResult;
import com.estofados.ecosidos.service.result.CustomerOrderMaterialAvailabilityResult;
import com.estofados.ecosidos.service.result.CustomerOrderSummaryResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private static final String FINISHED_PRODUCT_PART_TYPE_CODE = "FINISHED_PRODUCT";
    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final CustomerRepository customerRepository;
    private final CustomerOrderStatusRepository customerOrderStatusRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerOrderLineRepository customerOrderLineRepository;
    private final PartRepository partRepository;
    private final CustomerOrderMaterialService customerOrderMaterialService;
    private final StockReservationRepository stockReservationRepository;
    private final StockReservationStatusRepository stockReservationStatusRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final ManufacturingOrderStatusRepository manufacturingOrderStatusRepository;

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
        releaseCustomerOrderReservations(customerOrder);

        CustomerOrderStatus cancelledStatus = findStatus(CustomerOrderStatusCode.CANCELLED);
        customerOrder.setStatus(cancelledStatus);
        CustomerOrder savedCustomerOrder = customerOrderRepository.save(customerOrder);

        return toDetailResult(savedCustomerOrder);
    }

    @Transactional
    public CustomerOrderDetailResult reserveMaterials(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        CustomerOrder customerOrder = findCustomerOrderById(id);

        validateCanReserveMaterials(customerOrder);

        List<CustomerOrderMaterialAvailabilityResult> availability =
                customerOrderMaterialService.findMaterialAvailability(id);

        if (hasMissingMaterial(availability)) {
            CustomerOrderStatus waitingForMaterialStatus = findStatus(CustomerOrderStatusCode.WAITING_FOR_MATERIAL);
            customerOrder.setStatus(waitingForMaterialStatus);
            CustomerOrder savedCustomerOrder = customerOrderRepository.save(customerOrder);

            return toDetailResult(savedCustomerOrder);
        }

        StockReservationStatus reservedStatus = findStockReservationStatus(StockReservationStatusCode.RESERVED);

        int sequence = 1;
        for (CustomerOrderMaterialAvailabilityResult item : availability) {
            StockReservation reservation = createStockReservation(customerOrder, item, reservedStatus, sequence);
            stockReservationRepository.save(reservation);
            sequence++;
        }

        CustomerOrderStatus readyForProductionStatus = findStatus(CustomerOrderStatusCode.READY_FOR_PRODUCTION);
        customerOrder.setStatus(readyForProductionStatus);
        CustomerOrder savedCustomerOrder = customerOrderRepository.save(customerOrder);

        return toDetailResult(savedCustomerOrder);
    }

    @Transactional
    public CustomerOrderDetailResult startProduction(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        CustomerOrder customerOrder = findCustomerOrderById(id);

        validateCanStartProduction(customerOrder);

        List<CustomerOrderLine> lines = customerOrderLineRepository.findByCustomerOrderId(customerOrder.getId());
        ManufacturingOrderStatus openStatus = findManufacturingOrderStatus(ManufacturingOrderStatusCode.OPEN);

        int sequence = 1;
        LocalDateTime openedAt = LocalDateTime.now();
        for (CustomerOrderLine line : lines) {
            ManufacturingOrder manufacturingOrder = createManufacturingOrder(
                    customerOrder,
                    line,
                    openStatus,
                    openedAt,
                    sequence);
            manufacturingOrderRepository.save(manufacturingOrder);
            sequence++;
        }

        CustomerOrderStatus inPreparationStatus = findStatus(CustomerOrderStatusCode.IN_PREPARATION);
        customerOrder.setStatus(inPreparationStatus);
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

    private StockReservationStatus findStockReservationStatus(StockReservationStatusCode statusCode) {
        return stockReservationStatusRepository.findByCode(statusCode.name())
                .orElseThrow(() -> new IllegalStateException("Stock reservation status not found: "
                        + statusCode.name()));
    }

    private ManufacturingOrderStatus findManufacturingOrderStatus(ManufacturingOrderStatusCode statusCode) {
        return manufacturingOrderStatusRepository.findByCode(statusCode.name())
                .orElseThrow(() -> new IllegalStateException("Manufacturing order status not found: "
                        + statusCode.name()));
    }

    private CustomerOrder findCustomerOrderById(Long id) {
        return customerOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer order not found: " + id));
    }

    private RawMaterial findRawMaterialById(Long rawMaterialId) {
        return rawMaterialRepository.findById(rawMaterialId)
                .orElseThrow(() -> new IllegalArgumentException("Raw material not found: " + rawMaterialId));
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

    private void releaseCustomerOrderReservations(CustomerOrder customerOrder) {
        if (hasPartiallyConsumedReservations(customerOrder)) {
            throw new IllegalArgumentException("Customer order has partially consumed reservations and cannot be cancelled: "
                    + customerOrder.getId());
        }

        List<StockReservation> reservedReservations = findReservedStockReservations(customerOrder);
        if (reservedReservations.isEmpty()) {
            return;
        }

        StockReservationStatus releasedStatus = findStockReservationStatus(StockReservationStatusCode.RELEASED);
        LocalDateTime releasedAt = LocalDateTime.now();

        for (StockReservation reservation : reservedReservations) {
            reservation.setStatus(releasedStatus);
            reservation.setReleasedAt(releasedAt);
        }

        stockReservationRepository.saveAll(reservedReservations);
    }

    private List<StockReservation> findReservedStockReservations(CustomerOrder customerOrder) {
        return stockReservationRepository.findByCustomerOrderIdAndStatusCode(
                customerOrder.getId(),
                StockReservationStatusCode.RESERVED.name());
    }

    private boolean hasPartiallyConsumedReservations(CustomerOrder customerOrder) {
        return !stockReservationRepository.findByCustomerOrderIdAndStatusCode(
                        customerOrder.getId(),
                        StockReservationStatusCode.PARTIALLY_CONSUMED.name())
                .isEmpty();
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

    private void validateCanReserveMaterials(CustomerOrder customerOrder) {
        String currentStatusCode = getStatusCode(customerOrder);
        Long id = customerOrder.getId();

        if (CustomerOrderStatusCode.VALIDATED.name().equals(currentStatusCode)
                || CustomerOrderStatusCode.WAITING_FOR_MATERIAL.name().equals(currentStatusCode)) {
            return;
        }

        if (CustomerOrderStatusCode.READY_FOR_PRODUCTION.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Customer order materials are already reserved: " + id);
        }

        if (CustomerOrderStatusCode.CANCELLED.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Cancelled customer order cannot reserve materials: " + id);
        }

        if (CustomerOrderStatusCode.RECEIVED.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Customer order must be validated before reserving materials: " + id);
        }

        throw new IllegalArgumentException("Customer order cannot reserve materials from status: " + currentStatusCode);
    }

    private void validateCanStartProduction(CustomerOrder customerOrder) {
        String currentStatusCode = getStatusCode(customerOrder);
        Long id = customerOrder.getId();

        if (CustomerOrderStatusCode.READY_FOR_PRODUCTION.name().equals(currentStatusCode)) {
            return;
        }

        if (CustomerOrderStatusCode.RECEIVED.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Customer order must be validated before starting production: " + id);
        }

        if (CustomerOrderStatusCode.VALIDATED.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Customer order materials must be reserved before starting production: " + id);
        }

        if (CustomerOrderStatusCode.WAITING_FOR_MATERIAL.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Customer order is waiting for material and cannot start production: " + id);
        }

        if (CustomerOrderStatusCode.IN_PREPARATION.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Customer order production has already been started: " + id);
        }

        if (CustomerOrderStatusCode.CANCELLED.name().equals(currentStatusCode)) {
            throw new IllegalArgumentException("Cancelled customer order cannot start production: " + id);
        }

        throw new IllegalArgumentException("Customer order cannot start production from status: " + currentStatusCode);
    }

    private boolean hasMissingMaterial(List<CustomerOrderMaterialAvailabilityResult> availability) {
        return availability.stream()
                .anyMatch(item -> !item.available());
    }

    private StockReservation createStockReservation(
            CustomerOrder customerOrder,
            CustomerOrderMaterialAvailabilityResult item,
            StockReservationStatus reservedStatus,
            int sequence) {
        RawMaterial rawMaterial = findRawMaterialById(item.rawMaterialId());

        StockReservation reservation = new StockReservation();
        reservation.setCode(generateStockReservationCode(customerOrder, item, sequence));
        reservation.setCustomerOrder(customerOrder);
        reservation.setRawMaterial(rawMaterial);
        reservation.setStatus(reservedStatus);
        reservation.setReservedQuantity(item.requiredQuantity());
        reservation.setConsumedQuantity(BigDecimal.ZERO);
        reservation.setUnit(item.unit());
        reservation.setReservedAt(LocalDateTime.now());

        return reservation;
    }

    private ManufacturingOrder createManufacturingOrder(
            CustomerOrder customerOrder,
            CustomerOrderLine line,
            ManufacturingOrderStatus status,
            LocalDateTime openedAt,
            int sequence) {
        ManufacturingOrder manufacturingOrder = new ManufacturingOrder();
        manufacturingOrder.setCode(generateManufacturingOrderCode(customerOrder, line, sequence));
        manufacturingOrder.setStatus(status);
        manufacturingOrder.setCustomerOrder(customerOrder);
        manufacturingOrder.setCustomerOrderLine(line);
        manufacturingOrder.setPart(line.getPart());
        manufacturingOrder.setQuantity(line.getQuantity());
        manufacturingOrder.setOpenedAt(openedAt);

        return manufacturingOrder;
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

    private String generateStockReservationCode(
            CustomerOrder customerOrder,
            CustomerOrderMaterialAvailabilityResult item,
            int sequence) {
        return "SR-"
                + CODE_FORMATTER.format(LocalDateTime.now())
                + "-"
                + customerOrder.getId()
                + "-"
                + item.rawMaterialId()
                + "-"
                + sequence;
    }

    private String generateManufacturingOrderCode(CustomerOrder customerOrder, CustomerOrderLine line, int sequence) {
        return "MO-"
                + CODE_FORMATTER.format(LocalDateTime.now())
                + "-"
                + customerOrder.getId()
                + "-"
                + line.getId()
                + "-"
                + sequence;
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
}
