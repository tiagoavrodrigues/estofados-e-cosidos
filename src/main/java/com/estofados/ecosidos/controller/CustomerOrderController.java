package com.estofados.ecosidos.controller;

import com.estofados.ecosidos.dto.customerorder.CustomerOrderCreateRequest;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderLineCreateRequest;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderResponse;
import com.estofados.ecosidos.service.CustomerOrderService;
import com.estofados.ecosidos.service.input.CustomerOrderCreateInput;
import com.estofados.ecosidos.service.input.CustomerOrderLineCreateInput;
import com.estofados.ecosidos.service.result.CustomerOrderCreateResult;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @PostMapping
    public ResponseEntity<CustomerOrderResponse> create(@Valid @RequestBody CustomerOrderCreateRequest request) {
        CustomerOrderCreateInput input = toInput(request);
        CustomerOrderCreateResult result = customerOrderService.create(input);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    private CustomerOrderCreateInput toInput(CustomerOrderCreateRequest request) {
        List<CustomerOrderLineCreateInput> lines = request.lines().stream()
                .map(this::toLineInput)
                .toList();

        return new CustomerOrderCreateInput(
                request.customerId(),
                request.orderDate(),
                request.notes(),
                lines);
    }

    private CustomerOrderLineCreateInput toLineInput(CustomerOrderLineCreateRequest request) {
        return new CustomerOrderLineCreateInput(
                request.partId(),
                request.quantity());
    }

    private CustomerOrderResponse toResponse(CustomerOrderCreateResult result) {
        return new CustomerOrderResponse(
                result.id(),
                result.code(),
                result.customerId(),
                result.status(),
                result.orderDate(),
                result.notes());
    }
}
