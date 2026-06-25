package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.CustomerOrder;
import com.estofados.ecosidos.domain.CustomerOrderLine;
import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.ManufacturingOrderStatus;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.dto.common.PageResponse;
import com.estofados.ecosidos.repository.ManufacturingOrderRepository;
import com.estofados.ecosidos.service.result.ManufacturingOrderSummaryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManufacturingOrderService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ManufacturingOrderRepository manufacturingOrderRepository;

    @Transactional(readOnly = true)
    public PageResponse<ManufacturingOrderSummaryResult> findManufacturingOrders(int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "id"));

        Page<ManufacturingOrder> result = manufacturingOrderRepository.findAll(pageRequest);
        List<ManufacturingOrderSummaryResult> content = result.getContent().stream()
                .map(this::toSummaryResult)
                .toList();

        return new PageResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private int normalizePage(int page) {
        if (page < 0) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }

        if (size > MAX_SIZE) {
            return MAX_SIZE;
        }

        return size;
    }

    private ManufacturingOrderSummaryResult toSummaryResult(ManufacturingOrder manufacturingOrder) {
        CustomerOrder customerOrder = manufacturingOrder.getCustomerOrder();
        CustomerOrderLine customerOrderLine = manufacturingOrder.getCustomerOrderLine();
        Part part = manufacturingOrder.getPart();
        ManufacturingOrderStatus status = manufacturingOrder.getStatus();
        Long customerOrderId = customerOrder != null ? customerOrder.getId() : null;
        Long customerOrderLineId = customerOrderLine != null ? customerOrderLine.getId() : null;

        return new ManufacturingOrderSummaryResult(
                manufacturingOrder.getId(),
                manufacturingOrder.getCode(),
                customerOrderId,
                customerOrderLineId,
                part.getId(),
                part.getCode(),
                part.getName(),
                manufacturingOrder.getQuantity(),
                status.getCode(),
                manufacturingOrder.getOpenedAt(),
                manufacturingOrder.getCompletedAt());
    }
}
