package com.gregory.vehicleRentalAPI.maintenance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceRequest(

        @NotNull(message = "Vehicle Id is required")
        Long vehicleId,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Start date is required")
        LocalDateTime startDate,


        LocalDateTime endDate,

        @DecimalMin(value = "0.00", message = "Cost cannot be negative")
        BigDecimal cost
) {}
