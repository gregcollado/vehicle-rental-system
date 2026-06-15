package com.gregory.vehicleRentalAPI.shared.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
}