package com.gregory.vehicleRentalAPI.vehicle;

import com.gregory.vehicleRentalAPI.vehicle.dto.VehicleRequest;
import com.gregory.vehicleRentalAPI.vehicle.dto.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleRequest request, String createdByEmail);
    VehicleResponse getVehicleById(Long id);
    Page<VehicleResponse> getAllVehicle(Pageable pageable);
    VehicleResponse updateVehicle(Long id, VehicleRequest request);
    void deleteVehicle(Long id);
    Vehicle findById(Long id);
}

