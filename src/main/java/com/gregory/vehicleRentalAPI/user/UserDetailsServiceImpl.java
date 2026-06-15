package com.gregory.vehicleRentalAPI.user;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    // implements UserDetailsService → implementas el contrato que Spring exige

    private final UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        // Spring llama este método cuando necesita verificar un usuario
        // "username" en Spring = email en tu caso
        return userRepository.findByEmail(email)
                .map(usuario -> {
                    String authority = "ROLE_" + usuario.getRole().getName();
                    //System.out.println(">>> Authority generada: " + authority);
                    return User.builder()
                            .username(usuario.getEmail())
                            .password(usuario.getPassword())
                            .authorities(new SimpleGrantedAuthority(authority))
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
