package com.gregory.vehicleRentalAPI.maintenance;

import com.gregory.vehicleRentalAPI.maintenance.dto.MaintenanceRequest;
import com.gregory.vehicleRentalAPI.maintenance.dto.MaintenanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaintenanceService {

    MaintenanceResponse createMaintenance(MaintenanceRequest request, String createdByEmail);

    MaintenanceResponse getMaintenanceById(Long id);

    Page<MaintenanceResponse> getAllByStatus(MaintenanceStatus status, Pageable pageable);

    MaintenanceResponse getByVehicleId(Long vehicleId);

    MaintenanceResponse cancelMaintenance(Long id);

    MaintenanceResponse completeMaintenance(Long id);
}
