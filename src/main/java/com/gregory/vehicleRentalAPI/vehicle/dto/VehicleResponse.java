package com.gregory.vehicleRentalAPI.vehicle.dto;

import com.gregory.vehicleRentalAPI.vehicle.Vehicle;
import com.gregory.vehicleRentalAPI.vehicle.VehicleStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record VehicleResponse(
        Long id,
        String plateNumber,
        String brand,
        String model,
        Integer year,
        BigDecimal dailyRate,
        Integer mileage,
        VehicleStatus status
) {
    public static VehicleResponse from(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .dailyRate(vehicle.getDailyRate())
                .mileage(vehicle.getMileage())
                .status(vehicle.getStatus())
                .build();
    }
}
