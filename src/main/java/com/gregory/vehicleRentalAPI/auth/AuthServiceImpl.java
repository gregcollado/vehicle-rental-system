package com.gregory.vehicleRentalAPI.auth;

import com.gregory.vehicleRentalAPI.auth.dto.AuthResponse;
import com.gregory.vehicleRentalAPI.auth.dto.LoginRequest;
import com.gregory.vehicleRentalAPI.auth.dto.RegisterRequest;
import com.gregory.vehicleRentalAPI.role.Role;
import com.gregory.vehicleRentalAPI.role.RoleName;
import com.gregory.vehicleRentalAPI.role.RoleRepository;
import com.gregory.vehicleRentalAPI.security.JwtService;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceAlreadyExistsException;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceNotFoundException;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private void registerWithRole(RegisterRequest request, RoleName roleName) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setRole(role);

        userRepository.save(user);
    }

    @Transactional
    public void register(RegisterRequest request) {
        registerWithRole(request, RoleName.EMPLOYEE);
    }

    @Transactional
    public void registerAdmin(RegisterRequest request) {
        registerWithRole(request, RoleName.ADMIN);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(request.email());

        return new AuthResponse(token);
    }
}
