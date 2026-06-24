package com.estofados.ecosidos.controller;

import com.estofados.ecosidos.dto.customerorder.CustomerOrderCreateRequest;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderDetailResponse;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderLineCreateRequest;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderLineResponse;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderResponse;
import com.estofados.ecosidos.service.CustomerOrderService;
import com.estofados.ecosidos.service.input.CustomerOrderCreateInput;
import com.estofados.ecosidos.service.input.CustomerOrderLineCreateInput;
import com.estofados.ecosidos.service.result.CustomerOrderCreateResult;
import com.estofados.ecosidos.service.result.CustomerOrderDetailResult;
import com.estofados.ecosidos.service.result.CustomerOrderLineResult;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderDetailResponse> findById(@PathVariable Long id) {
        CustomerOrderDetailResult result = customerOrderService.findById(id);

        return ResponseEntity.ok(toDetailResponse(result));
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

    private CustomerOrderDetailResponse toDetailResponse(CustomerOrderDetailResult result) {
        List<CustomerOrderLineResponse> lines = result.lines().stream()
                .map(this::toLineResponse)
                .toList();

        return new CustomerOrderDetailResponse(
                result.id(),
                result.code(),
                result.customerId(),
                result.customerCode(),
                result.customerName(),
                result.status(),
                result.orderDate(),
                result.notes(),
                lines);
    }

    private CustomerOrderLineResponse toLineResponse(CustomerOrderLineResult result) {
        return new CustomerOrderLineResponse(
                result.id(),
                result.partId(),
                result.partCode(),
                result.partName(),
                result.quantity());
    }
}
