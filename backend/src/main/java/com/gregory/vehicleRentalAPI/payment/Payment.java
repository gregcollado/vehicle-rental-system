package com.gregory.vehicleRentalAPI.payment;


import com.gregory.vehicleRentalAPI.payment.enums.PaymentMethod;
import com.gregory.vehicleRentalAPI.payment.enums.PaymentStatus;
import com.gregory.vehicleRentalAPI.rental.Rental;
import com.gregory.vehicleRentalAPI.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Id;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "associated_rent", nullable = false)
     private Rental rental;


     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "created_by", nullable = false)
     private User createdBy;

     @Column(nullable = false)
     private BigDecimal amount;

     @Enumerated(EnumType.STRING)
     @Column(name = "payment_method", nullable = false)
     private PaymentMethod paymentMethod;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private PaymentStatus status;

     @Column
     private String reference;

     @CreationTimestamp
     @Column(name = "created_at", nullable = false, updatable = false)
     private LocalDateTime paymentDate;

}
