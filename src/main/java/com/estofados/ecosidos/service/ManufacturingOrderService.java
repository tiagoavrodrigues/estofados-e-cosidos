package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.CustomerOrder;
import com.estofados.ecosidos.domain.CustomerOrderLine;
import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.ManufacturingOrderStatus;
import com.estofados.ecosidos.domain.ManufacturingOrderStatusCode;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.dto.common.PageResponse;
import com.estofados.ecosidos.repository.ManufacturingOrderRepository;
import com.estofados.ecosidos.repository.ManufacturingOrderStatusRepository;
import com.estofados.ecosidos.service.result.ManufacturingOrderDetailResult;
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
    private final ManufacturingOrderStatusRepository manufacturingOrderStatusRepository;

    @Transactional(readOnly = true)
    public PageResponse<ManufacturingOrderSummaryResult> findManufacturingOrders(String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "id"));

        Page<ManufacturingOrder> result;
        if (status == null || status.isBlank()) {
            result = manufacturingOrderRepository.findAll(pageRequest);
        } else {
            String normalizedStatus = status.trim().toUpperCase();
            ManufacturingOrderStatus statusEntity = manufacturingOrderStatusRepository.findByCode(normalizedStatus)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Manufacturing order status not found: " + normalizedStatus));
            result = manufacturingOrderRepository.findByStatus(statusEntity, pageRequest);
        }

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

    @Transactional(readOnly = true)
    public ManufacturingOrderDetailResult findManufacturingOrder(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }

        ManufacturingOrder manufacturingOrder = manufacturingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + id));

        return toDetailResult(manufacturingOrder);
    }

    @Transactional
    public ManufacturingOrderDetailResult startCutting(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }

        ManufacturingOrder manufacturingOrder = manufacturingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + id));

        validateCanStartCutting(manufacturingOrder);

        ManufacturingOrderStatus inCuttingStatus = findStatus(ManufacturingOrderStatusCode.IN_CUTTING);
        manufacturingOrder.setStatus(inCuttingStatus);
        ManufacturingOrder savedManufacturingOrder = manufacturingOrderRepository.save(manufacturingOrder);

        return toDetailResult(savedManufacturingOrder);
    }

    @Transactional
    public ManufacturingOrderDetailResult confirmCut(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }

        ManufacturingOrder manufacturingOrder = manufacturingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + id));

        validateCanConfirmCut(manufacturingOrder);

        ManufacturingOrderStatus cutStatus = findStatus(ManufacturingOrderStatusCode.CUT);
        manufacturingOrder.setStatus(cutStatus);
        ManufacturingOrder savedManufacturingOrder = manufacturingOrderRepository.save(manufacturingOrder);

        return toDetailResult(savedManufacturingOrder);
    }

    private int normalizePage(int page) {
        if (page < 0) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    @Transactional
    public ManufacturingOrderDetailResult confirmTransformed(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }

        ManufacturingOrder manufacturingOrder = manufacturingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + id));

        validateCanConfirmTransformed(manufacturingOrder);

        ManufacturingOrderStatus transformedStatus = findStatus(ManufacturingOrderStatusCode.TRANSFORMED);
        manufacturingOrder.setStatus(transformedStatus);
        ManufacturingOrder savedManufacturingOrder = manufacturingOrderRepository.save(manufacturingOrder);

        return toDetailResult(savedManufacturingOrder);
    }

    @Transactional
    public ManufacturingOrderDetailResult confirmPrepared(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }

        ManufacturingOrder manufacturingOrder = manufacturingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + id));

        validateCanConfirmPrepared(manufacturingOrder);

        ManufacturingOrderStatus preparedStatus = findStatus(ManufacturingOrderStatusCode.PREPARED);
        manufacturingOrder.setStatus(preparedStatus);
        ManufacturingOrder savedManufacturingOrder = manufacturingOrderRepository.save(manufacturingOrder);

        return toDetailResult(savedManufacturingOrder);
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

    private ManufacturingOrderStatus findStatus(ManufacturingOrderStatusCode statusCode) {
        return manufacturingOrderStatusRepository.findByCode(statusCode.name())
                .orElseThrow(() -> new IllegalStateException("Manufacturing order status not found: "
                        + statusCode.name()));
    }

    private void validateCanStartCutting(ManufacturingOrder manufacturingOrder) {
        if (ManufacturingOrderStatusCode.OPEN.name().equals(getStatusCode(manufacturingOrder))) {
            return;
        }

        throw new IllegalArgumentException("Manufacturing order must be OPEN to start cutting: "
                + manufacturingOrder.getId());
    }

    private void validateCanConfirmCut(ManufacturingOrder manufacturingOrder) {
        if (ManufacturingOrderStatusCode.IN_CUTTING.name().equals(getStatusCode(manufacturingOrder))) {
            return;
        }

        throw new IllegalArgumentException("Manufacturing order must be IN_CUTTING to confirm cut: "
                + manufacturingOrder.getId());
    }

    private void validateCanConfirmTransformed(ManufacturingOrder manufacturingOrder) {
        if (ManufacturingOrderStatusCode.CUT.name().equals(getStatusCode(manufacturingOrder))) {
            return;
        }

        throw new IllegalArgumentException("Manufacturing order must be CUT to confirm transformed: "
                + manufacturingOrder.getId());
    }

    private void validateCanConfirmPrepared(ManufacturingOrder manufacturingOrder) {
        if (ManufacturingOrderStatusCode.TRANSFORMED.name().equals(getStatusCode(manufacturingOrder))) {
            return;
        }

        throw new IllegalArgumentException("Manufacturing order must be TRANSFORMED to confirm prepared: "
                + manufacturingOrder.getId());
    }

    private String getStatusCode(ManufacturingOrder manufacturingOrder) {
        if (manufacturingOrder.getStatus() == null) {
            return null;
        }

        return manufacturingOrder.getStatus().getCode();
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

    private ManufacturingOrderDetailResult toDetailResult(ManufacturingOrder manufacturingOrder) {
        CustomerOrder customerOrder = manufacturingOrder.getCustomerOrder();
        CustomerOrderLine customerOrderLine = manufacturingOrder.getCustomerOrderLine();
        Part part = manufacturingOrder.getPart();
        ManufacturingOrderStatus status = manufacturingOrder.getStatus();
        Long customerOrderId = customerOrder != null ? customerOrder.getId() : null;
        Long customerOrderLineId = customerOrderLine != null ? customerOrderLine.getId() : null;

        return new ManufacturingOrderDetailResult(
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
