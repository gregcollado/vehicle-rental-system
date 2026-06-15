package com.gregory.vehicleRentalAPI.payment;

import com.gregory.vehicleRentalAPI.payment.dto.PaymentRequest;
import com.gregory.vehicleRentalAPI.payment.dto.PaymentResponse;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentMethod;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentStatus;
import com.gregory.vehicleRentalAPI.rental.Rental;
import com.gregory.vehicleRentalAPI.rental.RentalService;
import com.gregory.vehicleRentalAPI.rental.RentalStatus;
import com.gregory.vehicleRentalAPI.shared.events.RentalCancelledEvent;
import com.gregory.vehicleRentalAPI.shared.exception.BusinessRuleException;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceNotFoundException;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service @RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RentalService rentalService;
    private final UserService userService;
    private final PaymentRepository paymentRepository;

    @Transactional @Override
    public PaymentResponse createPayment(PaymentRequest request, String createdByEmail){

        Rental rental = rentalService.findById(request.rentalId());
        User employee = userService.findByEmail(createdByEmail);

        Payment payment = Payment.builder()
                .createdBy(employee)
                .rental(rental)
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .reference(request.reference())
                .status(PaymentStatus.PENDING)
                .build();

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true) @Override
    public PaymentResponse getPaymentById(Long id){
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No payment was found with the ID: " + id));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true) @Override
    public Page<PaymentResponse> getPaymentsByRentalId(Long id, Pageable pageable){
        return paymentRepository.findAllByRentalId(id, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true) @Override
    public Page<PaymentResponse> getAllPayments(PaymentStatus status, Pageable pageable){
        return paymentRepository.findAllByStatus(status, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true) @Override
    public Page<PaymentResponse> getAllPayments(PaymentMethod method, Pageable pageable){
        return paymentRepository.findAllByPaymentMethod(method, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional @Override
    public PaymentResponse cancelPayment(Long id){
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment with id " + id + " not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only payment pending can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        return PaymentResponse.from(payment);
    }

    @Transactional @Override
    public PaymentResponse changePaymentToConfirmed(Long paymentId){
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(()-> new ResourceNotFoundException("Payment with id "+ paymentId+" not found"));

        if (payment.getStatus()!= PaymentStatus.PENDING)
            throw new BusinessRuleException("Payment status is not PENDING");

        Rental rental = rentalService.findById(payment.getRental().getId());

        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.OVERDUE)
            throw new BusinessRuleException("Cannot confirm payment for a rental with status " + rental.getStatus());

        payment.setStatus(PaymentStatus.CONFIRMED);

        List<Payment> payments =
                paymentRepository.findByRentalIdAndStatus(payment.getRental().getId(), PaymentStatus.CONFIRMED);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Payment confirmedPayment : payments)
            totalAmount = totalAmount.add(confirmedPayment.getAmount());

        rentalService.updateTotalAmount(rental.getId(),totalAmount);

        return PaymentResponse.from(payment);
    }

    @Transactional
    @EventListener
    public void onRentalCancelled(RentalCancelledEvent event) {
        // tu lógica aquí
        List<Payment> payments =
                paymentRepository.findByRentalIdAndStatus(event.rentalId(), PaymentStatus.PENDING);

        for (Payment payment : payments)
            payment.setStatus(PaymentStatus.CANCELLED);
    }
}
