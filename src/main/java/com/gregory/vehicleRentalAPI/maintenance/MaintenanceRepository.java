package com.gregory.vehicleRentalAPI.maintenance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface  MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    Optional<Maintenance> findByVehicleIdAndStatus(Long vehicleId, MaintenanceStatus status);

    boolean existsByVehicleIdAndStatus(Long vehicleId, MaintenanceStatus status);

    Page<Maintenance> findAllByStatus(MaintenanceStatus status, Pageable pageable);
}
