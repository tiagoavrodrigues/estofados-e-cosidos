package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.BillOfMaterialItem;
import com.estofados.ecosidos.domain.ManufacturingOrder;
import com.estofados.ecosidos.domain.ManufacturingOrderComponent;
import com.estofados.ecosidos.domain.Part;
import com.estofados.ecosidos.domain.PartTypeCode;
import com.estofados.ecosidos.repository.BillOfMaterialItemRepository;
import com.estofados.ecosidos.repository.ManufacturingOrderComponentRepository;
import com.estofados.ecosidos.repository.ManufacturingOrderRepository;
import com.estofados.ecosidos.service.result.ComponentSupplyStatusResult;
import com.estofados.ecosidos.service.result.ManufacturingOrderSupplyStatusResult;
import com.estofados.ecosidos.service.result.SuppliedSemiFinishedOrderResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManufacturingOrderSupplyStatusService {

    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final BillOfMaterialItemRepository billOfMaterialItemRepository;
    private final ManufacturingOrderComponentRepository manufacturingOrderComponentRepository;

    @Transactional(readOnly = true)
    public ManufacturingOrderSupplyStatusResult getSupplyStatus(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Manufacturing order id is required.");
        }

        ManufacturingOrder pa = manufacturingOrderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturing order not found: " + id));

        validateFinishedProduct(pa);

        List<BillOfMaterialItem> bomItems =
                billOfMaterialItemRepository.findByParentPartIdAndActiveTrue(pa.getPart().getId());

        if (bomItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Finished product has no active BOM: " + pa.getPart().getCode());
        }

        List<ManufacturingOrderComponent> links =
                manufacturingOrderComponentRepository.findByParentManufacturingOrder_IdAndDeletedAtIsNull(id);

        Map<Long, List<ManufacturingOrderComponent>> linksByPartId = links.stream()
                .collect(Collectors.groupingBy(
                        link -> link.getComponentManufacturingOrder().getPart().getId()));

        List<ComponentSupplyStatusResult> components = bomItems.stream()
                .map(bom -> toComponentResult(bom, pa.getQuantity(), linksByPartId))
                .toList();

        boolean fullySupplied = components.stream().allMatch(ComponentSupplyStatusResult::fullySupplied);

        Part part = pa.getPart();
        return new ManufacturingOrderSupplyStatusResult(
                pa.getId(),
                pa.getCode(),
                part.getId(),
                part.getCode(),
                part.getName(),
                pa.getQuantity(),
                fullySupplied,
                components);
    }

    private ComponentSupplyStatusResult toComponentResult(
            BillOfMaterialItem bom,
            BigDecimal paQuantity,
            Map<Long, List<ManufacturingOrderComponent>> linksByPartId) {

        Long componentPartId = bom.getComponentPart().getId();
        BigDecimal requiredPerUnit = bom.getQuantity();
        BigDecimal totalRequired = requiredPerUnit.multiply(paQuantity);

        List<ManufacturingOrderComponent> partLinks =
                linksByPartId.getOrDefault(componentPartId, List.of());

        BigDecimal suppliedQuantity = partLinks.stream()
                .map(link -> link.getComponentManufacturingOrder().getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal missingQuantity = totalRequired.subtract(suppliedQuantity).max(BigDecimal.ZERO);
        boolean fullySupplied = suppliedQuantity.compareTo(totalRequired) >= 0;

        List<SuppliedSemiFinishedOrderResult> suppliedOrders = partLinks.stream()
                .map(link -> new SuppliedSemiFinishedOrderResult(
                        link.getComponentManufacturingOrder().getId(),
                        link.getComponentManufacturingOrder().getCode(),
                        link.getComponentManufacturingOrder().getQuantity()))
                .toList();

        return new ComponentSupplyStatusResult(
                componentPartId,
                bom.getComponentPart().getCode(),
                bom.getComponentPart().getName(),
                requiredPerUnit,
                totalRequired,
                suppliedQuantity,
                missingQuantity,
                fullySupplied,
                suppliedOrders);
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
}
