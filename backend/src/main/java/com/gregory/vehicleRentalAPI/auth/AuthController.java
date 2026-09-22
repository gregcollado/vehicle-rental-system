package com.gregory.vehicleRentalAPI.auth;

import com.gregory.vehicleRentalAPI.auth.dto.AuthResponse;
import com.gregory.vehicleRentalAPI.auth.dto.LoginRequest;
import com.gregory.vehicleRentalAPI.shared.response.ApiResponse;
import com.gregory.vehicleRentalAPI.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro de usuarios y autenticación mediante JWT")
public class AuthController {

    private final AuthServiceImpl authService;

    // POST /auth/register → crea un nuevo usuario
    @Operation(
            summary = "Registrar nuevo empleado",
            description = "Crea una cuenta de usuario con rol EMPLOYEE. Endpoint público, no requiere autenticación."
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "User created successfully",
                        null
                ));
    }

    // POST /auth/admin/register → crea un nuevo usuario
    @Operation(
            summary = "Registrar nuevo ADMIN",
            description = "Crea una cuenta de usuario con rol ADMIN. Endpoint privado, requiere de otro usuario ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<Void>> registerAdmin(@Valid @RequestBody RegisterRequest request) {

        authService.registerAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Admin created successfully",
                        null
                ));
    }

    // POST /auth/login → verifica credenciales y devuelve token
    @Operation(
            summary = "Iniciar Sesion",
            description = "Verifica credenciales de los usuarios y retorna token que se usa en la autenticación"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Login successful",
                        authResponse
                )
        );
    }
}
