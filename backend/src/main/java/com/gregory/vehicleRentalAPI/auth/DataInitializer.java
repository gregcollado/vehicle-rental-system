package com.gregory.vehicleRentalAPI.auth;

import com.gregory.vehicleRentalAPI.role.Role;
import com.gregory.vehicleRentalAPI.role.RoleName;
import com.gregory.vehicleRentalAPI.role.RoleRepository;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceNotFoundException;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username}")
    private String adminUsername;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Override @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.findByName(RoleName.ADMIN).isEmpty()) {
            Role admin = new Role();
            admin.setName(RoleName.ADMIN);
            roleRepository.save(admin);
        }

        if (roleRepository.findByName(RoleName.EMPLOYEE).isEmpty()) {
            Role employee = new Role();
            employee.setName(RoleName.EMPLOYEE);
            roleRepository.save(employee);
        }

        if (userRepository.findByEmail(adminEmail).isEmpty()){
            Role admin = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(()->new ResourceNotFoundException("Not exists role ADMIN"));
            User adminDefault = User.builder()
                    .email(adminEmail)
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .active(true)
                    .role(admin)
                    .build();
            userRepository.save(adminDefault);
        }
    }
}