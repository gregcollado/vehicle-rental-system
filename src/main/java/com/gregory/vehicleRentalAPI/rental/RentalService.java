package com.gregory.vehicleRentalAPI.rental;

import com.gregory.vehicleRentalAPI.rental.dto.RentalRequest;
import com.gregory.vehicleRentalAPI.rental.dto.RentalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface RentalService {
    RentalResponse createRental(RentalRequest request, String createdByEmail);
    RentalResponse getRentalById(Long id);
    Page<RentalResponse> getAllRentals(RentalStatus status, Pageable pageable);
    RentalResponse returnVehicle(Long id, Integer mileage);
    RentalResponse cancelRental(Long id);
    Rental findById(Long id);
    void updateTotalAmount(Long id, BigDecimal totalAmount);
}
