package com.gregory.vehicleRentalAPI.customer;

import com.gregory.vehicleRentalAPI.customer.dto.CustomerRequest;
import com.gregory.vehicleRentalAPI.customer.dto.CustomerResponse;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@Tag(name = "Customer", description = "Registro, busqueda, actualizacion y eliminacion de clientes")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(
            summary = "Crear cliente",
            description = "Crea un nuevo cliente, devuelve response con los datos del cliente"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {

        CustomerResponse response = customerService.createCustomer(
                request, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Customer created successfully", response));
    }

    @Operation(
            summary = "Buscar cliente por ID",
            description = "Buscar un cliente por su id y devuelve sus datos en el response"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Customer retrieved successfully",
                        customerService.getCustomerById(id)));
    }

    @Operation(
            summary = "Consultar todos clientes",
            description = "Consulta todos los clientes y los devuelve en el response"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAll(
            @PageableDefault(size = 10, sort = "lastName", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Customers retrieved successfully",
                        customerService.getAllCustomers(pageable)));
    }

    @Operation(
            summary = "Actualizar datos de un cliente",
            description = "Actualiza los datos de un cliente y los devuelve en el response"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Customer updated successfully",
                        customerService.updateCustomer(id, request)));
    }

    @Operation(
            summary = "Cambiar estado de un cliente (active=false)",
            description = "Coloca un cliente en estado inactivo y devuelve mensaje de confirmación"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Customer deactivated successfully", null));
    }
}