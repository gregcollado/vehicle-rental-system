package com.gregory.vehicleRentalAPI.payment;

import com.gregory.vehicleRentalAPI.payment.dto.PaymentRequest;
import com.gregory.vehicleRentalAPI.payment.dto.PaymentResponse;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentMethod;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request, String createdByEmail);
    PaymentResponse getPaymentById(Long id);
    Page<PaymentResponse> getPaymentsByRentalId(Long id, Pageable pageable);
    Page<PaymentResponse> getAllPayments(PaymentStatus status, Pageable pageable);
    Page<PaymentResponse> getAllPayments(PaymentMethod status, Pageable pageable);
    PaymentResponse cancelPayment(Long id);
    PaymentResponse changePaymentToConfirmed(Long paymentId);
}
