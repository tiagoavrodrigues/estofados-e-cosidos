package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.Customer;
import com.estofados.ecosidos.domain.CustomerOrder;
import com.estofados.ecosidos.domain.CustomerOrderLine;
import com.estofados.ecosidos.domain.CustomerOrderStatus;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.repository.CustomerOrderLineRepository;
import com.estofados.ecosidos.repository.CustomerOrderRepository;
import com.estofados.ecosidos.repository.CustomerOrderStatusRepository;
import com.estofados.ecosidos.repository.CustomerRepository;
import com.estofados.ecosidos.repository.PartRepository;
import com.estofados.ecosidos.service.input.CustomerOrderCreateInput;
import com.estofados.ecosidos.service.input.CustomerOrderLineCreateInput;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private static final String RECEIVED_STATUS_CODE = "RECEIVED";
    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final CustomerRepository customerRepository;
    private final CustomerOrderStatusRepository customerOrderStatusRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerOrderLineRepository customerOrderLineRepository;
    private final PartRepository partRepository;

    @Transactional
    public CustomerOrder create(CustomerOrderCreateInput input) {
        validateInput(input);

        Customer customer = findCustomerById(input.customerId());
        CustomerOrderStatus receivedStatus = findReceivedStatus();

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

        return savedCustomerOrder;
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

    private CustomerOrderStatus findReceivedStatus() {
        return customerOrderStatusRepository.findByCode(RECEIVED_STATUS_CODE)
                .orElseThrow(() -> new IllegalStateException("Customer order status not found: "
                        + RECEIVED_STATUS_CODE));
    }

    private Part findPartById(Long partId) {
        return partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found: " + partId));
    }

    private String generateCode() {
        return "CO-" + CODE_FORMATTER.format(LocalDateTime.now());
    }

    private CustomerOrderLine createLine(CustomerOrder customerOrder, CustomerOrderLineCreateInput input) {
        Part part = findPartById(input.partId());

        CustomerOrderLine line = new CustomerOrderLine();
        line.setCustomerOrder(customerOrder);
        line.setPart(part);
        line.setQuantity(input.quantity());

        return line;
    }
}
