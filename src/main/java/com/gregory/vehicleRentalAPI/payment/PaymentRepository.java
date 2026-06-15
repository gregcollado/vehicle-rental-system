package com.gregory.vehicleRentalAPI.payment;

import com.gregory.vehicleRentalAPI.payment.enums.PaymentMethod;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


    // Una renta puede tener varios pagos → paginación
    Page<Payment> findAllByRentalId(Long rentalId, Pageable pageable);

    List<Payment> findByRentalIdAndStatus(Long id, PaymentStatus status);

    // Historial global por estado → paginación
    Page<Payment> findAllByStatus(PaymentStatus status, Pageable pageable);

    // Historial global por metodo → paginacion
    Page<Payment> findAllByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
}

