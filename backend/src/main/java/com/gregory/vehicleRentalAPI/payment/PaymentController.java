package com.gregory.vehicleRentalAPI.payment;

import com.gregory.vehicleRentalAPI.payment.dto.PaymentRequest;
import com.gregory.vehicleRentalAPI.payment.dto.PaymentResponse;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentMethod;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentStatus;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@Tag(name = "Payment", description = "Registro, consulta, confirmación y cancelación de pagos asociados a rentas")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Crear pago",
            description = "Registra un nuevo pago para una renta, con estado inicial PENDING"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @Valid @RequestBody PaymentRequest request, Authentication authentication
    ){
        PaymentResponse response = paymentService.createPayment(request, authentication.getName() );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payment created successfully", response));
    }

    @Operation(
            summary = "Buscar pago por ID",
            description = "Consulta un pago por su id y devuelve sus datos en el response"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payment retrieved successfully",
                        paymentService.getPaymentById(id)));
    }

    @Operation(
            summary = "Consultar pagos por estado",
            description = "Consulta los pagos filtrados por status (PENDING, CONFIRMED, REJECTED, CANCELLED), con paginación y orden por fecha de pago descendente"
    )
    @GetMapping("/by-status")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllByStatus(@RequestParam(defaultValue = "CONFIRMED")
                                                                             PaymentStatus status,
                                                                             @PageableDefault(size = 10, sort = "paymentDate", direction = Sort.Direction.DESC)
                                                                             Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payment retrieved successfully",
                        paymentService.getAllPayments(status, pageable)));
    }


    @Operation(
            summary = "Consultar pagos por método de pago",
            description = "Consulta los pagos filtrados por método (CASH, CARD, TRANSFER, CHECK), con paginación y orden por fecha de pago descendente"
    )
    @GetMapping("/by-method")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllByMethod(@RequestParam(defaultValue = "CASH")
                                                                             PaymentMethod method,
                                                                             @PageableDefault(size = 10, sort = "paymentDate", direction = Sort.Direction.DESC)
                                                                             Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payment retrieved successfully",
                        paymentService.getAllPayments(method, pageable)));
    }

    @Operation(
            summary = "Cancelar pago",
            description = "Cancela un pago. Solo aplica a pagos en estado PENDING"
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(@PathVariable Long id){

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payment cancelled successfully", paymentService.cancelPayment(id))
        );
    }

    @Operation(
            summary = "Consultar pagos por renta",
            description = "Consulta todos los pagos asociados a una renta específica, con paginación y orden por fecha de pago descendente"
    )
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getByRentalId(
            @PathVariable Long rentalId,
            @PageableDefault(size = 10, sort = "paymentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payments retrieved successfully",
                        paymentService.getPaymentsByRentalId(rentalId, pageable)));
    }

    @Operation(
            summary = "Confirmar pago",
            description = "Cambia el estado del pago a CONFIRMED. Solo aplica a pagos PENDING y la renta asociada debe estar ACTIVE u OVERDUE. " +
                    "Recalcula totalAmount de la renta sumando todos los pagos CONFIRMED"
    )
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> changeToConfirm(@PathVariable Long id){
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payment confirmed successfully",
                        paymentService.changePaymentToConfirmed(id)));
    }
}

