package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.Customer;
import com.estofados.ecosidos.domain.CustomerOrder;
import com.estofados.ecosidos.domain.CustomerOrderLine;
import com.estofados.ecosidos.domain.CustomerOrderStatus;
import com.estofados.ecosidos.domain.CustomerOrderStatusCode;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.repository.CustomerOrderLineRepository;
import com.estofados.ecosidos.repository.CustomerOrderRepository;
import com.estofados.ecosidos.repository.CustomerOrderStatusRepository;
import com.estofados.ecosidos.repository.CustomerRepository;
import com.estofados.ecosidos.repository.PartRepository;
import com.estofados.ecosidos.service.input.CustomerOrderCreateInput;
import com.estofados.ecosidos.service.input.CustomerOrderLineCreateInput;
import com.estofados.ecosidos.service.result.CustomerOrderCreateResult;
import com.estofados.ecosidos.service.result.CustomerOrderDetailResult;
import com.estofados.ecosidos.service.result.CustomerOrderLineResult;
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
