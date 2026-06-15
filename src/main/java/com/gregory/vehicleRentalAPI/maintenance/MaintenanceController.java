package com.gregory.vehicleRentalAPI.maintenance;

import com.gregory.vehicleRentalAPI.maintenance.dto.MaintenanceRequest;
import com.gregory.vehicleRentalAPI.maintenance.dto.MaintenanceResponse;
import com.gregory.vehicleRentalAPI.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@Tag(name = "Maintenance", description = "Registro, consulta, finalización y cancelación de mantenimientos de vehículos")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @Operation(
            summary = "Crear mantenimiento",
            description = "Registra un nuevo mantenimiento con estado inicial IN_PROGRESS. Requiere que el vehículo esté AVAILABLE. " +
                    "Cambia el vehículo a MAINTENANCE"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceResponse>> create(
            @Valid @RequestBody MaintenanceRequest request, Authentication authentication
    ){
        MaintenanceResponse response = maintenanceService.createMaintenance(request, authentication.getName() );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Maintenance created successfully", response));
    }

    @Operation(
            summary = "Buscar mantenimiento por ID",
            description = "Consulta un mantenimiento por su id y devuelve sus datos en el response"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Maintenance retrieved successfully",
                        maintenanceService.getMaintenanceById(id)));
    }

    @Operation(
            summary = "Consultar mantenimientos por estado",
            description = "Consulta los mantenimientos filtrados por status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED), con paginación y orden por fecha de creación descendente"
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Page<MaintenanceResponse>>> getAll(@RequestParam(defaultValue = "IN_PROGRESS")
                                                                         MaintenanceStatus status,
                                                                         @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                                         Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Maintenance retrieved successfully",
                        maintenanceService.getAllByStatus(status, pageable)));
    }

    @Operation(
            summary = "Verificar mantenimiento en progreso de un vehículo",
            description = "Consulta si un vehículo tiene actualmente un mantenimiento en estado IN_PROGRESS. " +
                    "No retorna el historial completo de mantenimientos del vehículo"
    )
    @GetMapping("/vehicle")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> getByVehicleId(@RequestParam Long vehicleId) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Maintenance retrieved successfully",
                        maintenanceService.getByVehicleId(vehicleId)));
    }

    @Operation(
            summary = "Cancelar mantenimiento",
            description = "Cancela un mantenimiento IN_PROGRESS y devuelve el vehículo a AVAILABLE"
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Maintenance cancelled successfully",
                        maintenanceService.cancelMaintenance(id)));
    }

    @Operation(
            summary = "Completar mantenimiento",
            description = "Marca un mantenimiento IN_PROGRESS como COMPLETED y devuelve el vehículo a AVAILABLE"
    )
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Maintenance completed successfully",
                        maintenanceService.completeMaintenance(id)));
    }
}