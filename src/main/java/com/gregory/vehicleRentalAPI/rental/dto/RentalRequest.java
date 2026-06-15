package com.gregory.vehicleRentalAPI.rental.dto;

import com.gregory.vehicleRentalAPI.customer.Customer;
import com.gregory.vehicleRentalAPI.vehicle.Vehicle;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RentalRequest (

    @NotNull(message = "Customer is required")
    Long customerId,

    @NotNull(message = "Vehicle is required")
    Long vehicleId,

    @NotNull(message = "Start date is required")
    LocalDateTime startDate,

    @NotNull(message = "Expected return date is required")
    LocalDateTime expectedReturnDate,

    String notes
){

}
