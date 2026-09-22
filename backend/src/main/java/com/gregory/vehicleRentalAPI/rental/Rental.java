package com.gregory.vehicleRentalAPI.rental;

import com.gregory.vehicleRentalAPI.customer.Customer;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Entity
@Getter @Setter
@Table(name="rentals")
@AllArgsConstructor @NoArgsConstructor
public class Rental {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "expected_return_date", nullable = false)
    private LocalDateTime expectedReturnDate;

    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private RentalStatus status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "daily_rate_snapshot", nullable = false)
    private BigDecimal dailyRateSnapshot;

    @Column(name = "expected_amount", nullable = false)
    private BigDecimal expectedAmount;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
