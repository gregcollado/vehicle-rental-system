package com.gregory.vehicleRentalAPI.customer.dto;

import com.gregory.vehicleRentalAPI.customer.Customer;
import lombok.Builder;

@Builder
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String licenseNumber,
        String phone,
        String email,
        String address,
        boolean active
) {
    public static CustomerResponse from(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .licenseNumber(customer.getLicenseNumber())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .active(customer.isActive())
                .build();
    }
}