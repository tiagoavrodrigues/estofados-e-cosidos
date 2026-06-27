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
import com.estofados.ecosidos.service.result.AvailableSemiFinishedOrderResult;
import com.estofados.ecosidos.service.result.ComponentAvailabilityResult;
import com.estofados.ecosidos.service.result.ManufacturingOrderComponentAvailabilityResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManufacturingOrderComponentAvailabilityService {

    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final BillOfMaterialItemRepository billOfMaterialItemRepository;
    private final ManufacturingOrderComponentRepository manufacturingOrderComponentRepository;
    private final RackLocationRepository rackLocationRepository;

    @Transactional(readOnly = true)
    public ManufacturingOrderComponentAvailabilityResult getComponentAvailability(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }

        ManufacturingOrder pa = manufacturingOrderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + id));

        validateFinishedProduct(pa);

        List<BillOfMaterialItem> bomItems =
                billOfMaterialItemRepository.findByParentPartIdAndActiveTrue(pa.getPart().getId());

        if (bomItems.isEmpty()) {
            return buildResult(pa, List.of());
        }

        List<Long> componentPartIds = bomItems.stream()
                .map(bom -> bom.getComponentPart().getId())
                .toList();

        Map<Long, BigDecimal> alreadyAssignedByPartId = calculateAlreadyAssigned(id, componentPartIds);

        List<ManufacturingOrder> preparedCandidates = manufacturingOrderRepository
                .findByPart_IdInAndStatus_CodeAndDeletedAtIsNull(
                        componentPartIds, ManufacturingOrderStatusCode.PREPARED.name());

        if (preparedCandidates.isEmpty()) {
            return buildResult(pa, toComponents(bomItems, pa.getQuantity(), alreadyAssignedByPartId, Map.of(), Map.of()));
        }

        List<Long> candidateIds = preparedCandidates.stream().map(ManufacturingOrder::getId).toList();

        Set<Long> alreadyLinkedIds = manufacturingOrderComponentRepository
                .findByComponentManufacturingOrder_IdInAndDeletedAtIsNull(candidateIds)
                .stream()
                .map(c -> c.getComponentManufacturingOrder().getId())
                .collect(Collectors.toSet());

        List<ManufacturingOrder> unlinkedCandidates = preparedCandidates.stream()
                .filter(mo -> !alreadyLinkedIds.contains(mo.getId()))
                .toList();

        if (unlinkedCandidates.isEmpty()) {
            return buildResult(pa, toComponents(bomItems, pa.getQuantity(), alreadyAssignedByPartId, Map.of(), Map.of()));
        }

        List<Long> unlinkedIds = unlinkedCandidates.stream().map(ManufacturingOrder::getId).toList();

        Map<Long, RackLocation> rackLocationByMoId = rackLocationRepository
                .findByManufacturingOrder_IdInAndRemovedAtIsNull(unlinkedIds)
                .stream()
                .collect(Collectors.toMap(rl -> rl.getManufacturingOrder().getId(), rl -> rl));

        Map<Long, List<ManufacturingOrder>> candidatesByPartId = unlinkedCandidates.stream()
                .filter(mo -> rackLocationByMoId.containsKey(mo.getId()))
                .collect(Collectors.groupingBy(mo -> mo.getPart().getId()));

        List<ComponentAvailabilityResult> components =
                toComponents(bomItems, pa.getQuantity(), alreadyAssignedByPartId, candidatesByPartId, rackLocationByMoId);

        return buildResult(pa, components);
    }

    private Map<Long, BigDecimal> calculateAlreadyAssigned(Long paId, List<Long> componentPartIds) {
        List<ManufacturingOrderComponent> existingLinks =
                manufacturingOrderComponentRepository.findByParentManufacturingOrder_IdAndDeletedAtIsNull(paId);

        return existingLinks.stream()
                .filter(link -> componentPartIds.contains(link.getComponentManufacturingOrder().getPart().getId()))
                .collect(Collectors.toMap(
                        link -> link.getComponentManufacturingOrder().getPart().getId(),
                        link -> link.getComponentManufacturingOrder().getQuantity(),
                        BigDecimal::add));
    }

    private List<ComponentAvailabilityResult> toComponents(
            List<BillOfMaterialItem> bomItems,
            BigDecimal paQuantity,
            Map<Long, BigDecimal> alreadyAssignedByPartId,
            Map<Long, List<ManufacturingOrder>> candidatesByPartId,
            Map<Long, RackLocation> rackLocationByMoId) {

        return bomItems.stream()
                .map(bom -> {
                    Long componentPartId = bom.getComponentPart().getId();
                    BigDecimal requiredPerUnit = bom.getQuantity();
                    BigDecimal total = requiredPerUnit.multiply(paQuantity);
                    BigDecimal assigned = alreadyAssignedByPartId.getOrDefault(componentPartId, BigDecimal.ZERO);
                    BigDecimal missing = total.subtract(assigned).max(BigDecimal.ZERO);

                    List<AvailableSemiFinishedOrderResult> available =
                            candidatesByPartId.getOrDefault(componentPartId, List.of()).stream()
                                    .map(mo -> toAvailableResult(mo, rackLocationByMoId.get(mo.getId())))
                                    .toList();

                    return new ComponentAvailabilityResult(
                            componentPartId,
                            bom.getComponentPart().getCode(),
                            bom.getComponentPart().getName(),
                            requiredPerUnit,
                            total,
                            assigned,
                            missing,
                            available);
                })
                .toList();
    }

    private void validateFinishedProduct(ManufacturingOrder pa) {
        Part part = pa.getPart();
        String partTypeCode = part.getPartType() != null ? part.getPartType().getCode() : null;
        if (!PartTypeCode.FINISHED_PRODUCT.name().equals(partTypeCode)) {
            throw new IllegalArgumentException(
                    "Manufacturing order must be of type FINISHED_PRODUCT to check component availability. "
                            + "Current part type: " + partTypeCode);
        }
    }

    private AvailableSemiFinishedOrderResult toAvailableResult(ManufacturingOrder mo, RackLocation rackLocation) {
        return new AvailableSemiFinishedOrderResult(
                mo.getId(),
                mo.getCode(),
                mo.getQuantity(),
                rackLocation.getId(),
                rackLocation.getRack().getId(),
                rackLocation.getRack().getCode(),
                rackLocation.getRack().getName(),
                rackLocation.getLocatedAt());
    }

    private ManufacturingOrderComponentAvailabilityResult buildResult(
            ManufacturingOrder pa, List<ComponentAvailabilityResult> components) {
        Part part = pa.getPart();
        return new ManufacturingOrderComponentAvailabilityResult(
                pa.getId(),
                pa.getCode(),
                part.getId(),
                part.getCode(),
                part.getName(),
                pa.getQuantity(),
                components);
    }
}
