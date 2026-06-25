package com.estofados.ecosidos.service;

import com.estofados.ecosidos.domain.CustomerOrder;
import com.estofados.ecosidos.domain.CustomerOrderLine;
import com.estofados.ecosidos.domain.PartRawMaterialRequirement;
import com.estofados.ecosidos.domain.RawMaterial;
import com.estofados.ecosidos.domain.Stock;
import com.estofados.ecosidos.domain.StockReservation;
import com.estofados.ecosidos.domain.StockReservationStatusCode;
import com.estofados.ecosidos.domain.StockStatusCode;
import com.estofados.ecosidos.repository.BillOfMaterialItemRepository;
import com.estofados.ecosidos.repository.CustomerOrderLineRepository;
import com.estofados.ecosidos.repository.CustomerOrderRepository;
import com.estofados.ecosidos.repository.PartRawMaterialRequirementRepository;
import com.estofados.ecosidos.repository.StockRepository;
import com.estofados.ecosidos.repository.StockReservationRepository;
import com.estofados.ecosidos.service.result.CustomerOrderMaterialAvailabilityResult;
import com.estofados.ecosidos.service.result.CustomerOrderMaterialRequirementResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOrderMaterialService {

    private static final List<String> ACTIVE_STOCK_RESERVATION_STATUS_CODES = List.of(
            StockReservationStatusCode.RESERVED.name(),
            StockReservationStatusCode.PARTIALLY_CONSUMED.name());
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int CALCULATION_SCALE = 6;
    private static final RoundingMode CALCULATION_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerOrderLineRepository customerOrderLineRepository;
    private final BillOfMaterialItemRepository billOfMaterialItemRepository;
    private final PartRawMaterialRequirementRepository partRawMaterialRequirementRepository;
    private final StockRepository stockRepository;
    private final StockReservationRepository stockReservationRepository;

    @Transactional(readOnly = true)
    public List<CustomerOrderMaterialRequirementResult> findMaterialRequirements(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        CustomerOrder customerOrder = findCustomerOrderById(id);
        Map<MaterialRequirementKey, MaterialRequirementAccumulator> requirements = new HashMap<>();

        for (CustomerOrderLine line : customerOrderLineRepository.findByCustomerOrderId(customerOrder.getId())) {
            addLineMaterialRequirements(line, requirements);
        }

        return requirements.values().stream()
                .map(MaterialRequirementAccumulator::toResult)
                .sorted(Comparator.comparing(CustomerOrderMaterialRequirementResult::rawMaterialCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderMaterialAvailabilityResult> findMaterialAvailability(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer order id is required.");
        }

        return findMaterialRequirements(id).stream()
                .map(this::toMaterialAvailabilityResult)
                .toList();
    }

    private CustomerOrder findCustomerOrderById(Long id) {
        return customerOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer order not found: " + id));
    }

    private void addLineMaterialRequirements(
            CustomerOrderLine line,
            Map<MaterialRequirementKey, MaterialRequirementAccumulator> requirements) {
        for (var billOfMaterialItem : billOfMaterialItemRepository.findByParentPartIdAndActiveTrue(line.getPart().getId())) {
            BigDecimal componentQuantity = line.getQuantity().multiply(billOfMaterialItem.getQuantity());

            for (PartRawMaterialRequirement requirement
                    : partRawMaterialRequirementRepository.findByPartIdAndActiveTrue(
                            billOfMaterialItem.getComponentPart().getId())) {
                BigDecimal requiredQuantity = componentQuantity
                        .multiply(requirement.getQuantity())
                        .multiply(wasteFactor(requirement.getWastePercentage()));
                RawMaterial rawMaterial = requirement.getRawMaterial();
                MaterialRequirementKey key = new MaterialRequirementKey(rawMaterial.getId(), requirement.getUnit());

                requirements.computeIfAbsent(
                                key,
                                ignored -> new MaterialRequirementAccumulator(
                                        rawMaterial.getId(),
                                        rawMaterial.getCode(),
                                        rawMaterial.getName(),
                                        requirement.getUnit()))
                        .add(requiredQuantity);
            }
        }
    }

    private BigDecimal wasteFactor(BigDecimal wastePercentage) {
        if (wastePercentage == null) {
            return BigDecimal.ONE;
        }

        return BigDecimal.ONE.add(
                wastePercentage.divide(ONE_HUNDRED, CALCULATION_SCALE, CALCULATION_ROUNDING_MODE));
    }

    private CustomerOrderMaterialAvailabilityResult toMaterialAvailabilityResult(
            CustomerOrderMaterialRequirementResult requirement) {
        BigDecimal availableQuantity = calculateAvailableQuantity(requirement.rawMaterialId(), requirement.unit());
        BigDecimal missingQuantity = max(requirement.requiredQuantity().subtract(availableQuantity), BigDecimal.ZERO);

        return new CustomerOrderMaterialAvailabilityResult(
                requirement.rawMaterialId(),
                requirement.rawMaterialCode(),
                requirement.rawMaterialName(),
                requirement.requiredQuantity(),
                availableQuantity,
                missingQuantity,
                requirement.unit(),
                missingQuantity.compareTo(BigDecimal.ZERO) == 0);
    }

    private BigDecimal calculateAvailableQuantity(Long rawMaterialId, String unit) {
        BigDecimal stockQuantity = stockRepository
                .findByRawMaterialIdAndUnitAndStockStatusCode(rawMaterialId, unit, StockStatusCode.AVAILABLE.name()).stream()
                .map(Stock::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal reservedQuantity = stockReservationRepository
                .findByRawMaterialIdAndUnitAndStatusCodeIn(
                        rawMaterialId,
                        unit,
                        ACTIVE_STOCK_RESERVATION_STATUS_CODES)
                .stream()
                .map(this::remainingReservedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return max(stockQuantity.subtract(reservedQuantity), BigDecimal.ZERO);
    }

    private BigDecimal remainingReservedQuantity(StockReservation reservation) {
        BigDecimal reservedQuantity = reservation.getReservedQuantity() != null
                ? reservation.getReservedQuantity()
                : BigDecimal.ZERO;
        BigDecimal consumedQuantity = reservation.getConsumedQuantity() != null
                ? reservation.getConsumedQuantity()
                : BigDecimal.ZERO;

        return max(reservedQuantity.subtract(consumedQuantity), BigDecimal.ZERO);
    }

    private BigDecimal max(BigDecimal first, BigDecimal second) {
        if (first.compareTo(second) >= 0) {
            return first;
        }

        return second;
    }

    private record MaterialRequirementKey(Long rawMaterialId, String unit) {
    }

    private static class MaterialRequirementAccumulator {

        private final Long rawMaterialId;
        private final String rawMaterialCode;
        private final String rawMaterialName;
        private final String unit;
        private BigDecimal requiredQuantity = BigDecimal.ZERO;

        private MaterialRequirementAccumulator(
                Long rawMaterialId,
                String rawMaterialCode,
                String rawMaterialName,
                String unit) {
            this.rawMaterialId = rawMaterialId;
            this.rawMaterialCode = rawMaterialCode;
            this.rawMaterialName = rawMaterialName;
            this.unit = unit;
        }

        private void add(BigDecimal quantity) {
            requiredQuantity = requiredQuantity.add(quantity);
        }

        private CustomerOrderMaterialRequirementResult toResult() {
            return new CustomerOrderMaterialRequirementResult(
                    rawMaterialId,
                    rawMaterialCode,
                    rawMaterialName,
                    requiredQuantity,
                    unit);
        }
    }
}
