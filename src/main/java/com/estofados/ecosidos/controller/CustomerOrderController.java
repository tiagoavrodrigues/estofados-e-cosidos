package com.estofados.ecosidos.controller;

import com.estofados.ecosidos.dto.common.PageResponse;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderCreateRequest;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderDetailResponse;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderLineCreateRequest;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderLineResponse;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderResponse;
import com.estofados.ecosidos.dto.customerorder.CustomerOrderSummaryResponse;
import com.estofados.ecosidos.service.CustomerOrderService;
import com.estofados.ecosidos.service.input.CustomerOrderCreateInput;
import com.estofados.ecosidos.service.input.CustomerOrderLineCreateInput;
import com.estofados.ecosidos.service.result.CustomerOrderCreateResult;
import com.estofados.ecosidos.service.result.CustomerOrderDetailResult;
import com.estofados.ecosidos.service.result.CustomerOrderLineResult;
import com.estofados.ecosidos.service.result.CustomerOrderSummaryResult;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public ResponseEntity<PageResponse<CustomerOrderSummaryResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        validatePagination(page, size);

        Page<CustomerOrderSummaryResult> result = customerOrderService.findAll(PageRequest.of(page, size));

        return ResponseEntity.ok(toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderDetailResponse> findById(@PathVariable Long id) {
        CustomerOrderDetailResult result = customerOrderService.findById(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<CustomerOrderDetailResponse> validate(@PathVariable Long id) {
        CustomerOrderDetailResult result = customerOrderService.validate(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CustomerOrderDetailResponse> cancel(@PathVariable Long id) {
        CustomerOrderDetailResult result = customerOrderService.cancel(id);

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

    private PageResponse<CustomerOrderSummaryResponse> toPageResponse(Page<CustomerOrderSummaryResult> result) {
        List<CustomerOrderSummaryResponse> content = result.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();

        return new PageResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private CustomerOrderSummaryResponse toSummaryResponse(CustomerOrderSummaryResult result) {
        return new CustomerOrderSummaryResponse(
                result.id(),
                result.code(),
                result.customerId(),
                result.customerCode(),
                result.customerName(),
                result.status(),
                result.orderDate());
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

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative.");
        }

        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }

        if (size > 100) {
            throw new IllegalArgumentException("Page size must not be greater than 100.");
        }
    }
}
