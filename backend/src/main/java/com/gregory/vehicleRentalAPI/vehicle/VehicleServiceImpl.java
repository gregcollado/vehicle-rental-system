package com.gregory.vehicleRentalAPI.vehicle;


import com.gregory.vehicleRentalAPI.vehicle.dto.VehicleResponse;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceAlreadyExistsException;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceNotFoundException;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.user.UserService;
import com.gregory.vehicleRentalAPI.vehicle.dto.VehicleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    @Override
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request, String createdByEmail) {

        Optional<Vehicle> existing = vehicleRepository.findByPlateNumber(request.plateNumber());

        if (existing.isPresent()) {
            Vehicle vehicle = existing.get();
            if (vehicle.getStatus() != VehicleStatus.INACTIVE) {
                throw new ResourceAlreadyExistsException(
                        "A vehicle with plate number " + request.plateNumber() + " already exists");
            }
            // Reactiva y actualiza
            vehicle.setBrand(request.brand());
            vehicle.setModel(request.model());
            vehicle.setYear(request.year());
            vehicle.setMileage(request.mileage());
            vehicle.setDailyRate(request.dailyRate());
            vehicle.setStatus(VehicleStatus.AVAILABLE);

            return VehicleResponse.from(vehicle);
        }

        User employee = userService.findByEmail(createdByEmail);

        Vehicle vehicle = Vehicle.builder()
                .createdBy(employee)
                .status(VehicleStatus.AVAILABLE)
                .brand(request.brand())
                .plateNumber(request.plateNumber())
                .model(request.model())
                .year(request.year())
                .mileage(request.mileage())
                .dailyRate(request.dailyRate())
                .build();

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }



    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id){
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Vehicle with id " + id + " not found"));

        return VehicleResponse.from(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getAllVehicle(Pageable pageable){
        return vehicleRepository.findAllByStatusNot(VehicleStatus.INACTIVE, pageable)
                .map(VehicleResponse::from);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request){
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle with id " + id + " not found"));

        if (vehicleRepository.existsByPlateNumberAndIdNot(request.plateNumber(), id)) {
            throw new ResourceAlreadyExistsException(
                    "A vehicle with plate number " + request.plateNumber() + " already exists");
        }

        vehicle.setBrand(request.brand());
        vehicle.setMileage(request.mileage());
        vehicle.setModel(request.model());
        vehicle.setDailyRate(request.dailyRate());
        vehicle.setPlateNumber(request.plateNumber());
        vehicle.setYear(request.year());

        return VehicleResponse.from(vehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long id){
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle with id " + id + " not found"));

        vehicle.setStatus(VehicleStatus.INACTIVE);
    }

    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle with id " + id + " not found"));
    }

}
