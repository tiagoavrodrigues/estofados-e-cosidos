package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.BillOfMaterialItem;
import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.ManufacturingOrderComponent;
import com.estofados.ecosidos.domain.ManufacturingOrderStatusCode;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.domain.PartTypeCode;
import com.estofados.ecosidos.domain.RackLocation;
import com.estofados.ecosidos.repository.BillOfMaterialItemRepository;
import com.estofados.ecosidos.repository.ManufacturingOrderComponentRepository;
import com.estofados.ecosidos.repository.ManufacturingOrderRepository;
import com.estofados.ecosidos.repository.RackLocationRepository;
import com.estofados.ecosidos.service.result.ComponentSupplyResult;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManufacturingOrderComponentSupplyService {

    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final BillOfMaterialItemRepository billOfMaterialItemRepository;
    private final ManufacturingOrderComponentRepository manufacturingOrderComponentRepository;
    private final RackLocationRepository rackLocationRepository;

    @Transactional
    public ComponentSupplyResult supplyComponent(Long paId, Long saId) {
        if (paId == null) {
            throw new IllegalArgumentException("Parent manufacturing order id is required.");
        }
        if (saId == null) {
            throw new IllegalArgumentException("Semi-finished manufacturing order id is required.");
        }

        ManufacturingOrder pa = manufacturingOrderRepository.findByIdAndDeletedAtIsNull(paId)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + paId));

        validateFinishedProduct(pa);

        ManufacturingOrder sa = manufacturingOrderRepository.findByIdAndDeletedAtIsNull(saId)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + saId));

        validateSemiFinished(sa);
        validatePrepared(sa);
        validateInBom(pa, sa);
        validateNotAlreadyAssigned(saId);

        RackLocation rackLocation = rackLocationRepository.findByManufacturingOrderAndRemovedAtIsNull(sa)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Semi-finished manufacturing order has no active rack location: " + saId));

        rackLocation.setRemovedAt(LocalDateTime.now());
        rackLocationRepository.save(rackLocation);

        ManufacturingOrderComponent component = new ManufacturingOrderComponent();
        component.setParentManufacturingOrder(pa);
        component.setComponentManufacturingOrder(sa);
        ManufacturingOrderComponent saved = manufacturingOrderComponentRepository.save(component);

        Part saPart = sa.getPart();
        return new ComponentSupplyResult(
                saved.getId(),
                pa.getId(),
                pa.getCode(),
                sa.getId(),
                sa.getCode(),
                saPart.getId(),
                saPart.getCode(),
                saPart.getName());
    }

    private void validateFinishedProduct(ManufacturingOrder pa) {
        String partTypeCode = pa.getPart().getPartType() != null
                ? pa.getPart().getPartType().getCode()
                : null;
        if (!PartTypeCode.FINISHED_PRODUCT.name().equals(partTypeCode)) {
            throw new IllegalArgumentException(
                    "Manufacturing order must be of type FINISHED_PRODUCT: " + pa.getId());
        }
    }

    private void validateSemiFinished(ManufacturingOrder sa) {
        String partTypeCode = sa.getPart().getPartType() != null
                ? sa.getPart().getPartType().getCode()
                : null;
        if (!PartTypeCode.SEMI_FINISHED.name().equals(partTypeCode)) {
            throw new IllegalArgumentException(
                    "Manufacturing order must be of type SEMI_FINISHED: " + sa.getId());
        }
    }

    private void validatePrepared(ManufacturingOrder sa) {
        String statusCode = sa.getStatus() != null ? sa.getStatus().getCode() : null;
        if (!ManufacturingOrderStatusCode.PREPARED.name().equals(statusCode)) {
            throw new IllegalArgumentException(
                    "Semi-finished manufacturing order must be PREPARED. Current status: " + statusCode);
        }
    }

    private void validateInBom(ManufacturingOrder pa, ManufacturingOrder sa) {
        List<BillOfMaterialItem> bomItems =
                billOfMaterialItemRepository.findByParentPartIdAndActiveTrue(pa.getPart().getId());

        boolean inBom = bomItems.stream()
                .anyMatch(bom -> bom.getComponentPart().getId().equals(sa.getPart().getId()));

        if (!inBom) {
            throw new IllegalArgumentException(
                    "Semi-finished part is not part of the BOM for this finished product. "
                            + "SA part: " + sa.getPart().getCode()
                            + ", PA part: " + pa.getPart().getCode());
        }
    }

    private void validateNotAlreadyAssigned(Long saId) {
        if (manufacturingOrderComponentRepository
                .existsByComponentManufacturingOrder_IdAndDeletedAtIsNull(saId)) {
            throw new IllegalArgumentException(
                    "Semi-finished manufacturing order is already assigned to a finished product order: " + saId);
        }
    }
}
