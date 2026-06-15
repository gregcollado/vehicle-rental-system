package com.gregory.vehicleRentalAPI.rental;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    Page<Rental> findByStatus(RentalStatus status, Pageable pageable);

    boolean existsByVehicleIdAndStatus(Long vehicleId, RentalStatus status);

}
