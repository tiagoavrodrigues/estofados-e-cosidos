package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.ManufacturingOrderStatusCode;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.domain.PartTypeCode;
import com.estofados.ecosidos.domain.Rack;
import com.estofados.ecosidos.domain.RackLocation;
import com.estofados.ecosidos.repository.ManufacturingOrderRepository;
import com.estofados.ecosidos.repository.RackLocationRepository;
import com.estofados.ecosidos.repository.RackRepository;
import com.estofados.ecosidos.service.result.RackLocationResult;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RackLocationService {

    private final RackLocationRepository rackLocationRepository;
    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final RackRepository rackRepository;

    @Transactional
    public RackLocationResult locateManufacturingOrder(Long manufacturingOrderId, Long rackId) {
        if (manufacturingOrderId == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }
        if (rackId == null) {
            throw new IllegalArgumentException("Rack id is required.");
        }

        ManufacturingOrder manufacturingOrder = manufacturingOrderRepository.findById(manufacturingOrderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Manufacturing order not found: " + manufacturingOrderId));

        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new IllegalArgumentException("Rack not found: " + rackId));

        validateRackActive(rack);
        validateManufacturingOrderPrepared(manufacturingOrder);
        validateManufacturingOrderSemiFinished(manufacturingOrder);
        validateNoActiveRackLocationForManufacturingOrder(manufacturingOrder);
        validateNoActiveRackLocationForRack(rack);

        RackLocation rackLocation = new RackLocation();
        rackLocation.setManufacturingOrder(manufacturingOrder);
        rackLocation.setRack(rack);
        rackLocation.setLocatedAt(LocalDateTime.now());

        RackLocation saved = rackLocationRepository.save(rackLocation);
        return toResult(saved);
    }

    @Transactional(readOnly = true)
    public List<RackLocationResult> findActiveRackLocations() {
        return rackLocationRepository.findByRemovedAtIsNullOrderByIdAsc().stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional
    public RackLocationResult moveRackLocation(Long rackLocationId, Long newRackId) {
        if (rackLocationId == null) {
            throw new IllegalArgumentException("Rack location id is required.");
        }

        RackLocation current = rackLocationRepository.findById(rackLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Rack location not found: " + rackLocationId));

        if (current.getRemovedAt() != null) {
            throw new IllegalArgumentException("Rack location is not active: " + rackLocationId);
        }

        if (newRackId == null) {
            throw new IllegalArgumentException("Rack id is required.");
        }

        Rack newRack = rackRepository.findById(newRackId)
                .orElseThrow(() -> new IllegalArgumentException("Rack not found: " + newRackId));

        validateRackActive(newRack);

        if (current.getRack().getId().equals(newRackId)) {
            throw new IllegalArgumentException("Manufacturing order is already located in rack: " + current.getRack().getId());
        }

        validateNoActiveRackLocationForRack(newRack);

        ManufacturingOrder manufacturingOrder = current.getManufacturingOrder();
        validateManufacturingOrderPrepared(manufacturingOrder);
        validateManufacturingOrderSemiFinished(manufacturingOrder);

        current.setRemovedAt(LocalDateTime.now());
        rackLocationRepository.save(current);

        RackLocation next = new RackLocation();
        next.setManufacturingOrder(manufacturingOrder);
        next.setRack(newRack);
        next.setLocatedAt(LocalDateTime.now());

        RackLocation saved = rackLocationRepository.save(next);
        return toResult(saved);
    }

    private void validateRackActive(Rack rack) {
        if (!Boolean.TRUE.equals(rack.getActive())) {
            throw new IllegalArgumentException("Rack is not active: " + rack.getCode());
        }
    }

    private void validateManufacturingOrderPrepared(ManufacturingOrder manufacturingOrder) {
        String statusCode = manufacturingOrder.getStatus() != null
                ? manufacturingOrder.getStatus().getCode()
                : null;
        if (!ManufacturingOrderStatusCode.PREPARED.name().equals(statusCode)) {
            throw new IllegalArgumentException(
                    "Manufacturing order must be PREPARED to be located in rack. Current status: " + statusCode);
        }
    }

    private void validateManufacturingOrderSemiFinished(ManufacturingOrder manufacturingOrder) {
        Part part = manufacturingOrder.getPart();
        String partTypeCode = part.getPartType() != null ? part.getPartType().getCode() : null;
        if (!PartTypeCode.SEMI_FINISHED.name().equals(partTypeCode)) {
            throw new IllegalArgumentException(
                    "Manufacturing order must be of type SEMI_FINISHED to be located in rack: " + partTypeCode);
        }
    }

    private void validateNoActiveRackLocationForManufacturingOrder(ManufacturingOrder manufacturingOrder) {
        rackLocationRepository.findByManufacturingOrderAndRemovedAtIsNull(manufacturingOrder)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Manufacturing order is already located in rack: " + existing.getRack().getCode());
                });
    }

    private void validateNoActiveRackLocationForRack(Rack rack) {
        rackLocationRepository.findByRackAndRemovedAtIsNull(rack)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Rack already is already occupied by manufacturing order: " + existing.getManufacturingOrder().getCode());
                });
    }

    private RackLocationResult toResult(RackLocation rackLocation) {
        Rack rack = rackLocation.getRack();
        ManufacturingOrder manufacturingOrder = rackLocation.getManufacturingOrder();
        Part part = manufacturingOrder.getPart();

        return new RackLocationResult(
                rackLocation.getId(),
                rack.getId(),
                rack.getCode(),
                rack.getName(),
                manufacturingOrder.getId(),
                manufacturingOrder.getCode(),
                part.getId(),
                part.getCode(),
                part.getName(),
                manufacturingOrder.getQuantity(),
                rackLocation.getLocatedAt());
    }
}
