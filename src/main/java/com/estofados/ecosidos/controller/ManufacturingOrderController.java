package com.estofados.ecosidos.controller;

import com.estofados.ecosidos.dto.common.PageResponse;
import com.estofados.ecosidos.dto.manufacturingorder.AvailableSemiFinishedOrderResponse;
import com.estofados.ecosidos.dto.manufacturingorder.ComponentAvailabilityResponse;
import com.estofados.ecosidos.dto.manufacturingorder.ManufacturingOrderComponentAvailabilityResponse;
import com.estofados.ecosidos.dto.manufacturingorder.ManufacturingOrderDetailResponse;
import com.estofados.ecosidos.dto.manufacturingorder.ManufacturingOrderSummaryResponse;
import com.estofados.ecosidos.service.ManufacturingOrderComponentAvailabilityService;
import com.estofados.ecosidos.service.ManufacturingOrderService;
import com.estofados.ecosidos.service.result.AvailableSemiFinishedOrderResult;
import com.estofados.ecosidos.service.result.ComponentAvailabilityResult;
import com.estofados.ecosidos.service.result.ManufacturingOrderComponentAvailabilityResult;
import com.estofados.ecosidos.service.result.ManufacturingOrderDetailResult;
import com.estofados.ecosidos.service.result.ManufacturingOrderSummaryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manufacturing-orders")
@RequiredArgsConstructor
public class ManufacturingOrderController {

    private final ManufacturingOrderService manufacturingOrderService;
    private final ManufacturingOrderComponentAvailabilityService componentAvailabilityService;

    @GetMapping
    public ResponseEntity<PageResponse<ManufacturingOrderSummaryResponse>> findManufacturingOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ManufacturingOrderSummaryResult> result =
                manufacturingOrderService.findManufacturingOrders(status, page, size);

        return ResponseEntity.ok(toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManufacturingOrderDetailResponse> findManufacturingOrder(@PathVariable Long id) {
        ManufacturingOrderDetailResult result = manufacturingOrderService.findManufacturingOrder(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PostMapping("/{id}/start-cutting")
    public ResponseEntity<ManufacturingOrderDetailResponse> startCutting(@PathVariable Long id) {
        ManufacturingOrderDetailResult result = manufacturingOrderService.startCutting(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PostMapping("/{id}/confirm-cut")
    public ResponseEntity<ManufacturingOrderDetailResponse> confirmCut(@PathVariable Long id) {
        ManufacturingOrderDetailResult result = manufacturingOrderService.confirmCut(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PostMapping("/{id}/confirm-transformed")
    public ResponseEntity<ManufacturingOrderDetailResponse> confirmTransformed(@PathVariable Long id) {
        ManufacturingOrderDetailResult result = manufacturingOrderService.confirmTransformed(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PostMapping("/{id}/confirm-prepared")
    public ResponseEntity<ManufacturingOrderDetailResponse> confirmPrepared(@PathVariable Long id) {
        ManufacturingOrderDetailResult result = manufacturingOrderService.confirmPrepared(id);

        return ResponseEntity.ok(toDetailResponse(result));
    }

    @GetMapping("/{id}/component-availability")
    public ResponseEntity<ManufacturingOrderComponentAvailabilityResponse> getComponentAvailability(
            @PathVariable Long id) {
        ManufacturingOrderComponentAvailabilityResult result =
                componentAvailabilityService.getComponentAvailability(id);

        return ResponseEntity.ok(toComponentAvailabilityResponse(result));
    }

    private ManufacturingOrderComponentAvailabilityResponse toComponentAvailabilityResponse(
            ManufacturingOrderComponentAvailabilityResult result) {
        List<ComponentAvailabilityResponse> components = result.components().stream()
                .map(this::toComponentAvailabilityResponse)
                .toList();

        return new ManufacturingOrderComponentAvailabilityResponse(
                result.manufacturingOrderId(),
                result.manufacturingOrderCode(),
                result.partId(),
                result.partCode(),
                result.partName(),
                result.quantity(),
                components);
    }

    private ComponentAvailabilityResponse toComponentAvailabilityResponse(ComponentAvailabilityResult result) {
        List<AvailableSemiFinishedOrderResponse> available = result.availableSemiFinishedOrders().stream()
                .map(this::toAvailableSemiFinishedOrderResponse)
                .toList();

        return new ComponentAvailabilityResponse(
                result.componentPartId(),
                result.componentPartCode(),
                result.componentPartName(),
                result.requiredQuantityPerUnit(),
                result.totalRequiredQuantity(),
                result.alreadyAssignedQuantity(),
                result.missingQuantity(),
                available);
    }

    private AvailableSemiFinishedOrderResponse toAvailableSemiFinishedOrderResponse(
            AvailableSemiFinishedOrderResult result) {
        return new AvailableSemiFinishedOrderResponse(
                result.manufacturingOrderId(),
                result.manufacturingOrderCode(),
                result.quantity(),
                result.rackLocationId(),
                result.rackId(),
                result.rackCode(),
                result.rackName(),
                result.locatedAt());
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
