package com.gregory.vehicleRentalAPI.security;

import com.gregory.vehicleRentalAPI.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter garantiza que este filtro
    // se ejecuta exactamente UNA vez por petición

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        //System.out.println(">>> Authorization header: " + authHeader);

        // 1. Busca el header "Authorization" en la petición
        //String authHeader = request.getHeader("Authorization");

        // 2. Si no hay header o no empieza con "Bearer ", deja pasar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrae el token quitando "Bearer " (7 caracteres)
        String token = authHeader.substring(7);

        // 4. Extrae el email que está dentro del token
        String email = jwtService.extractEmail(token);

        // 5. Si hay email y el usuario aún no está autenticado en esta petición
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Busca el usuario en la BD por su email
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 7. Verifica que el token sea válido
            if (jwtService.isTokenValid(token, userDetails)) {

                // 8. Marca al usuario como autenticado en Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Continúa con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    // Este metodo le dice al filtro que ignore las rutas públicas
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/auth/register") || path.equals("/auth/login");
    }
}
