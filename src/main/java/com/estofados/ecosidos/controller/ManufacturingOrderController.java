package com.estofados.ecosidos.controller;

import com.estofados.ecosidos.dto.common.PageResponse;
import com.estofados.ecosidos.dto.manufacturingorder.ManufacturingOrderDetailResponse;
import com.estofados.ecosidos.dto.manufacturingorder.ManufacturingOrderSummaryResponse;
import com.estofados.ecosidos.service.ManufacturingOrderService;
import com.estofados.ecosidos.service.result.ManufacturingOrderDetailResult;
import com.estofados.ecosidos.service.result.ManufacturingOrderSummaryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manufacturing-orders")
@RequiredArgsConstructor
public class ManufacturingOrderController {

    private final ManufacturingOrderService manufacturingOrderService;

    @GetMapping
    public ResponseEntity<PageResponse<ManufacturingOrderSummaryResponse>> findManufacturingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ManufacturingOrderSummaryResult> result =
                manufacturingOrderService.findManufacturingOrders(page, size);

        return ResponseEntity.ok(toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManufacturingOrderDetailResponse> findManufacturingOrder(@PathVariable Long id) {
        ManufacturingOrderDetailResult result = manufacturingOrderService.findManufacturingOrder(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    private PageResponse<ManufacturingOrderSummaryResponse> toPageResponse(
            PageResponse<ManufacturingOrderSummaryResult> result) {
        List<ManufacturingOrderSummaryResponse> content = result.content().stream()
                .map(this::toSummaryResponse)
                .toList();

        return new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    private ManufacturingOrderSummaryResponse toSummaryResponse(ManufacturingOrderSummaryResult result) {
        return new ManufacturingOrderSummaryResponse(
                result.id(),
                result.code(),
                result.customerOrderId(),
                result.customerOrderLineId(),
                result.partId(),
                result.partCode(),
                result.partName(),
                result.quantity(),
                result.status(),
                result.openedAt(),
                result.completedAt());
    }

    private ManufacturingOrderDetailResponse toDetailResponse(ManufacturingOrderDetailResult result) {
        return new ManufacturingOrderDetailResponse(
                result.id(),
                result.code(),
                result.customerOrderId(),
                result.customerOrderLineId(),
                result.partId(),
                result.partCode(),
                result.partName(),
                result.quantity(),
                result.status(),
                result.openedAt(),
                result.completedAt());
    }
}
