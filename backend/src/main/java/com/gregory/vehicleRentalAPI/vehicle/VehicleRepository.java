package com.gregory.vehicleRentalAPI.vehicle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndIdNot(String plateNumber, Long id);

    Page<Vehicle> findAllByStatusNot(VehicleStatus status, Pageable pageable);

    Optional<Vehicle> findByPlateNumber(String plateNumber);
}
