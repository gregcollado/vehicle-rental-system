package com.gregory.vehicleRentalAPI.rental;

import com.gregory.vehicleRentalAPI.rental.dto.RentalRequest;
import com.gregory.vehicleRentalAPI.rental.dto.RentalResponse;
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
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@Tag(name = "Rental", description = "Creación, consulta, devolución y cancelación de rentas de vehículos")
public class RentalController {

    private final RentalService rentalService;

    @Operation(
            summary = "Crear renta",
            description = "Crea una nueva renta. Requiere que el vehículo esté AVAILABLE y el cliente esté activo. " +
                    "Calcula automáticamente expectedAmount (dailyRate × días) y guarda un snapshot de la tarifa diaria. " +
                    "El mínimo de renta es 1 día. Cambia el vehículo a RENTED"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RentalResponse>> create(
            @Valid @RequestBody RentalRequest request, Authentication authentication
    ){
        RentalResponse response = rentalService.createRental(request, authentication.getName() );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Rental created successfully", response));
    }

    @Operation(
            summary = "Buscar renta por ID",
            description = "Consulta una renta por su id y devuelve sus datos en el response"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RentalResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Rental retrieved successfully",
                        rentalService.getRentalById(id)));
    }

    @Operation(
            summary = "Consultar rentas por estado",
            description = "Consulta las rentas filtradas por status (ACTIVE, COMPLETED, CANCELLED, OVERDUE), con paginación y orden por fecha de creación descendente"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RentalResponse>>> getAll( @RequestParam(defaultValue = "ACTIVE")
                                                                     RentalStatus status,
                                                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                                     Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Rental retrieved successfully",
                        rentalService.getAllRentals(status, pageable)));
    }

    @Operation(
            summary = "Registrar devolución del vehículo",
            description = "Marca la renta como COMPLETED y el vehículo como AVAILABLE. Solo aplica a rentas ACTIVE u OVERDUE. " +
                    "Requiere que totalAmount sea mayor o igual a expectedAmount; de lo contrario se rechaza por saldo pendiente"
    )
    @PatchMapping("/{id}/return")
    public ResponseEntity<ApiResponse<RentalResponse>> returnVehicle(@PathVariable Long id,
                                                                     @RequestParam Integer mileage){
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Vehicle return successfully ",
                        rentalService.returnVehicle(id, mileage))
        );
    }

    @Operation(
            summary = "Cancelar renta",
            description = "Cancela una renta ACTIVE u OVERDUE y libera el vehículo (vuelve a AVAILABLE). " +
                    "Cancela automáticamente cualquier pago PENDING asociado a la renta"
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RentalResponse>> cancelRental(@PathVariable Long id){

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Rental cancelled successfully", rentalService.cancelRental(id))
        );
    }
}