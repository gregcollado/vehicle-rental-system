package com.gregory.vehicleRentalAPI.vehicle;

import com.gregory.vehicleRentalAPI.shared.response.ApiResponse;
import com.gregory.vehicleRentalAPI.vehicle.dto.VehicleRequest;
import com.gregory.vehicleRentalAPI.vehicle.dto.VehicleResponse;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@Tag(name = "Vehicle", description = "Registro, búsqueda, actualización y desactivación de vehículos")
public class VehicleController
{
    private final VehicleService vehicleService;

    @Operation(
            summary = "Crear vehículo",
            description = "Registra un nuevo vehículo con estado inicial AVAILABLE"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> create(
            @Valid @RequestBody VehicleRequest request,
            Authentication authentication) {

        VehicleResponse response = vehicleService.createVehicle(
                request, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Vehicle created successfully", response));
    }

    @Operation(
            summary = "Buscar vehículo por ID",
            description = "Consulta un vehículo por su id y devuelve sus datos en el response"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Vehicle retrieved successfully",
                        vehicleService.getVehicleById(id)));
    }

    @Operation(
            summary = "Consultar todos los vehículos",
            description = "Consulta todos los vehículos registrados, con soporte de paginación y orden por marca"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getAll(
            @PageableDefault(size = 10, sort = "brand", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Vehicle retrieved successfully",
                        vehicleService.getAllVehicle(pageable)));
    }

    @Operation(
            summary = "Actualizar datos de un vehículo",
            description = "Actualiza los datos de un vehículo y los devuelve en el response. No modifica su status operativo"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Vehicle updated successfully",
                        vehicleService.updateVehicle(id, request)));
    }

    @Operation(
            summary = "Desactivar un vehículo (status=INACTIVE)",
            description = "Coloca un vehículo en estado INACTIVO. A diferencia de Customer, Vehicle no usa borrado lógico con campo booleano: la baja se representa mediante VehicleStatus.INACTIVE"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Vehicle deactivated successfully", null));
    }
}