package com.gregory.vehicleRentalAPI.rental.dto;

import com.gregory.vehicleRentalAPI.rental.Rental;
import com.gregory.vehicleRentalAPI.rental.RentalStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record RentalResponse(
        Long id,
        String customerFullName,
        String vehicle,
        LocalDateTime startDate,
        LocalDateTime expectedReturnDate,
        LocalDateTime actualReturnDate,
        RentalStatus status,
        BigDecimal totalAmount,
        String notes,
        LocalDateTime createdAt
) {
    public static RentalResponse from(Rental rental){
        return RentalResponse.builder()
                .id(rental.getId())
                .customerFullName(rental.getCustomer().getFirstName() + " " + rental.getCustomer().getLastName())
                .vehicle(rental.getVehicle().getPlateNumber() + " " + rental.getVehicle().getBrand() + " " + rental.getVehicle().getModel())
                .startDate(rental.getStartDate())
                .expectedReturnDate(rental.getExpectedReturnDate())
                .actualReturnDate(rental.getActualReturnDate())
                .status(rental.getStatus())
                .totalAmount(rental.getTotalAmount())
                .notes(rental.getNotes())
                .createdAt(rental.getCreatedAt())
                .build();
    }
}
