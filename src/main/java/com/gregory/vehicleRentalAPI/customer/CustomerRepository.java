package com.gregory.vehicleRentalAPI.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<Customer> findByLicenseNumber(String licenceNumber);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);

    Page<Customer> findAllByActiveTrue(Pageable pageable);
}
