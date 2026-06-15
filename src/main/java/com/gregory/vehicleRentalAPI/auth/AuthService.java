package com.gregory.vehicleRentalAPI.auth;

import com.gregory.vehicleRentalAPI.auth.dto.AuthResponse;
import com.gregory.vehicleRentalAPI.auth.dto.LoginRequest;
import com.gregory.vehicleRentalAPI.auth.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    void registerAdmin(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}