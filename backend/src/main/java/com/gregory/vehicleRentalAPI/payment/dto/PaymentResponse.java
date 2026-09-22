package com.gregory.vehicleRentalAPI.payment.dto;

import com.gregory.vehicleRentalAPI.payment.Payment;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentMethod;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentResponse(
        Long id,
        Long rentalID,
        String createdBy,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String reference,
        LocalDateTime paymentDate
) {
    public static PaymentResponse from(Payment payment){
        return PaymentResponse.builder()
                .id(payment.getId())
                .rentalID(payment.getRental().getId())
                .createdBy(payment.getCreatedBy().getUsername())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .reference(payment.getReference())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
