package com.gregory.vehicleRentalAPI.maintenance;

import com.gregory.vehicleRentalAPI.maintenance.dto.MaintenanceRequest;
import com.gregory.vehicleRentalAPI.maintenance.dto.MaintenanceResponse;
import com.gregory.vehicleRentalAPI.shared.exception.BusinessRuleException;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceNotFoundException;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.user.UserService;
import com.gregory.vehicleRentalAPI.vehicle.Vehicle;
import com.gregory.vehicleRentalAPI.vehicle.VehicleService;
import com.gregory.vehicleRentalAPI.vehicle.VehicleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final VehicleService vehicleService;
    private final UserService userService;
    private final MaintenanceRepository maintenanceRepository;

    @Transactional
    @Override
    public MaintenanceResponse createMaintenance(MaintenanceRequest request, String createdByEmail){

        Vehicle vehicle = vehicleService.findById(request.vehicleId());
        if(vehicle.getStatus() != VehicleStatus.AVAILABLE)
            throw  new BusinessRuleException("Vehicle with id "+ request.vehicleId() +" is not available for maintenance");

        User employee = userService.findByEmail(createdByEmail);

        Maintenance maintenance = Maintenance.builder()
                .vehicle(vehicle)
                .createdBy(employee)
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .cost(request.cost())
                .status(MaintenanceStatus.IN_PROGRESS)
                .build();

        vehicle.setStatus(VehicleStatus.MAINTENANCE);

        return MaintenanceResponse.from(maintenanceRepository.save(maintenance));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MaintenanceResponse> getAllByStatus(MaintenanceStatus status, Pageable pageable){

        return maintenanceRepository.findAllByStatus(status, pageable)
                .map(MaintenanceResponse::from);
    }

    @Transactional(readOnly = true)
    @Override
    public MaintenanceResponse getByVehicleId(Long vehicleId){

        Maintenance maintenance = maintenanceRepository.findByVehicleIdAndStatus(vehicleId, MaintenanceStatus.IN_PROGRESS)
                .orElseThrow(()-> new ResourceNotFoundException("No maintenance in progress was found with the vehicle ID: " + vehicleId));

        return MaintenanceResponse.from(maintenance);
    }

    @Transactional(readOnly = true)
    @Override
    public MaintenanceResponse getMaintenanceById(Long id){

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No maintenance was found with the ID: " + id));

        return MaintenanceResponse.from(maintenance);
    }

    @Transactional
    @Override
    public MaintenanceResponse cancelMaintenance(Long id){

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No maintenance was found with the ID: " + id));

        if (maintenance.getStatus() != MaintenanceStatus.IN_PROGRESS){
             throw new BusinessRuleException("No maintenance in progress was found with the ID: " + id);
        }

        Vehicle vehicle = vehicleService.findById(maintenance.getVehicle().getId());

        maintenance.setStatus(MaintenanceStatus.CANCELLED);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        return MaintenanceResponse.from(maintenance);
    }

    @Transactional
    @Override
    public MaintenanceResponse completeMaintenance(Long id){

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No maintenance was found with the ID: " + id));

        if (maintenance.getStatus() != MaintenanceStatus.IN_PROGRESS)
            throw new BusinessRuleException("Only in progress maintenances can be completed");

        Vehicle vehicle = vehicleService.findById(maintenance.getVehicle().getId());

        vehicle.setStatus(VehicleStatus.AVAILABLE);
        maintenance.setStatus(MaintenanceStatus.COMPLETED);

        return MaintenanceResponse.from(maintenance);
    }

}
