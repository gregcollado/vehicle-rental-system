package com.gregory.vehicleRentalAPI.customer.dto;

import jakarta.validation.constraints.*;

public record CustomerRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 80, message = "First name cannot exceed 80 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 80, message = "Last name cannot exceed 80 characters")
        String lastName,

        @NotBlank(message = "License number is required")
        @Size(max = 30, message = "License number cannot exceed 30 characters")
        String licenseNumber,

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone cannot exceed 20 characters")
        String phone,

        @Email(message = "Invalid email format")
        @Size(max = 120, message = "Email cannot exceed 120 characters")
        String email,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address
) {}
