package com.gregory.vehicleRentalAPI.maintenance.dto;

import com.gregory.vehicleRentalAPI.maintenance.Maintenance;
import com.gregory.vehicleRentalAPI.maintenance.MaintenanceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MaintenanceResponse(
        Long id,
        String vehicle,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal cost,
        MaintenanceStatus status

) {
    public static MaintenanceResponse from(Maintenance maintenance){
        return MaintenanceResponse.builder()
                .id(maintenance.getId())
                .vehicle(maintenance.getVehicle().getPlateNumber()+" "+maintenance.getVehicle().getBrand()
                        +" "+maintenance.getVehicle().getModel())
                .description(maintenance.getDescription())
                .startDate(maintenance.getStartDate())
                .endDate(maintenance.getEndDate())
                .cost(maintenance.getCost())
                .status(maintenance.getStatus())
                .build();
    }
}
