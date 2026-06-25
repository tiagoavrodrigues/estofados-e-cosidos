package com.estofados.ecosidos.controller;

import com.estofados.ecosidos.dto.racklocation.MoveRackLocationRequest;
import com.estofados.ecosidos.dto.racklocation.RackLocationRequest;
import com.estofados.ecosidos.dto.racklocation.RackLocationResponse;
import com.estofados.ecosidos.service.RackLocationService;
import com.estofados.ecosidos.service.result.RackLocationResult;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rack-locations")
@RequiredArgsConstructor
public class RackLocationController {

    private final RackLocationService rackLocationService;

    @PostMapping
    public ResponseEntity<RackLocationResponse> locateManufacturingOrder(
            @Valid @RequestBody RackLocationRequest request) {
        RackLocationResult result = rackLocationService.locateManufacturingOrder(
                request.manufacturingOrderId(), request.rackId());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @GetMapping
    public ResponseEntity<List<RackLocationResponse>> findActiveRackLocations() {
        List<RackLocationResponse> response = rackLocationService.findActiveRackLocations().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<RackLocationResponse> moveRackLocation(
            @PathVariable Long id,
            @Valid @RequestBody MoveRackLocationRequest request) {
        RackLocationResult result = rackLocationService.moveRackLocation(id, request.rackId());

        return ResponseEntity.ok(toResponse(result));
    }

    private RackLocationResponse toResponse(RackLocationResult result) {
        return new RackLocationResponse(
                result.rackLocationId(),
                result.rackId(),
                result.rackCode(),
                result.rackName(),
                result.manufacturingOrderId(),
                result.manufacturingOrderCode(),
                result.partId(),
                result.partCode(),
                result.partName(),
                result.quantity(),
                result.locatedAt());
    }
}
