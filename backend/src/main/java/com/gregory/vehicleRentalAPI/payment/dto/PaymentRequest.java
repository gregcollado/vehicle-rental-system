package com.gregory.vehicleRentalAPI.payment.dto;

import com.gregory.vehicleRentalAPI.payment.enums.PaymentMethod;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentStatus;
import com.gregory.vehicleRentalAPI.rental.Rental;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotNull(message = "Rental ID is required")
        Long rentalId,

        @NotNull(message = "Amount ID is required")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String reference
) {
}
