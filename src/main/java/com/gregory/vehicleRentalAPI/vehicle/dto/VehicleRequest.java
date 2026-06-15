package com.gregory.vehicleRentalAPI.vehicle.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record VehicleRequest(

        @NotBlank(message = "Plate number is required")
        @Size(max=20, message = "Plate number cannot exceed 20 characters")
        String plateNumber,

        @NotBlank(message = "Brand is required")
        @Size(max=50, message = "Brand cannot exceed 50 characters")
        String brand,

        @NotBlank(message = "Model is required")
        @Size(max=50, message = "model cannot exceed 50 characters")
        String model,

        @NotNull(message = "Year is required")
        @Min(value = 1900, message = "Year must be valid")
        @Max(value = 2100, message = "Year must be valid")
        Integer year,

        @NotNull(message = "Mileage is required")
        @Min(value = 0, message = "Mileage cannot be negative")
        Integer mileage,

        @NotNull(message = "Daily rate is required")
        @DecimalMin(value = "0.01", message = "Daily rate must be greater than zero")
        BigDecimal dailyRate
) {
}
