package com.gregory.vehicleRentalAPI.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Rental Management API")
                        .description("API REST para gestión de un negocio de renta de vehículos. " +
                                "Incluye módulos de Users, Customers, Vehicles, Rentals, Payments y Maintenance " +
                                "con operaciones CRUD y lógica de negocio (cálculo de montos, control de estados, " +
                                "reactivación de clientes, etc). Autenticación mediante Spring Security y JWT con roles ADMIN/EMPLOYEE.")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}