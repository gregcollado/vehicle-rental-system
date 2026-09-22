package com.gregory.vehicleRentalAPI.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Genera un token con el email del usuario adentro
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)          // guarda el email en el payload
                .issuedAt(new Date())    // cuando fue creado
                .expiration(new Date(System.currentTimeMillis() + expiration)) // cuando vence
                .signWith(getKey())      // firma con la clave secreta
                .compact();
    }

    // Extrae el email que está guardado dentro del token
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Verifica que el token sea válido y no haya expirado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Verifica si el token ya expiró
    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    // Convierte la clave secreta en un formato que entiende la librería JWT
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}