package com.mindhub.api_gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public AdminFilter adminFilter() {
        return new AdminFilter(jwtUtils);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service-api-auth", r -> r.path("/api/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                                .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                                .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                                .addResponseHeader("X-Response-Source", "user-service") // identifica el microservicio
                                .circuitBreaker(c -> c.setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service") // Ruta al fallback
                                        .setRouteId("user-service-fallback")))
                        .uri("lb://user-service"))
                // Ruta especial para comunicación entre order-service y user-service
                .route("user-service-api-internal", r -> r.path("/api/internal/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Request", "true") // Header para identificar solicitudes internas
                                .addResponseHeader("X-Response-Source", "user-service") // Identifica el microservicio
                                .circuitBreaker(c -> c.setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service")
                                        .setRouteId("user-service-fallback")))
                        .uri("lb://user-service")) // Balanceador de carga para user-service
                .route("product-service-api-internal", r -> r.path("/api/internal/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Request", "true") // Header para identificar solicitudes internas
                                .addResponseHeader("X-Response-Source", "product-service") // Identifica el microservicio
                                .circuitBreaker(c -> c.setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product-service")
                                        .setRouteId("product-service-fallback")))
                        .uri("lb://product-service")) // Balanceador de carga para user-service
                .route("user-service-api-user", r -> r.path("/api/user/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter())
                                .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                                .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                                .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                                .addResponseHeader("X-Response-Source", "user-service") // identifica el microservicio
                                .circuitBreaker(c -> c.setName("userServiceCircuitBreaker") // Configura un circuito para manejar fallos. Si un servicio no está disponible, se redirige a una ruta de fallback.
                                        .setFallbackUri("forward:/fallback/user-service") // Ruta al fallback
                                        .setRouteId("user-service-fallback")))
                        .uri("lb://user-service"))
                .route("user-service-api-admin", r -> r.path("/api/admin/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter())
                                .filter(adminFilter())
                                .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                                .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                                .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                                .addResponseHeader("X-Response-Source", "user-service") // identifica el microservicio
                                .circuitBreaker(c -> c.setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service") // Ruta al fallback
                                        .setRouteId("user-service-fallback")))
                        .uri("lb://user-service"))
                .route("product-service-user", r -> r.path("/api/products/public/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter())
                                .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                                .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                                .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                                .addResponseHeader("X-Response-Source", "product-service") // identifica el microservicio
                                .circuitBreaker(c -> c.setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product-service")
                                        .setRouteId("product-service-fallback")))
                        .uri("lb://product-service"))
                .route("product-service-admin", r -> r.path("/api/products/admin/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter())
                                .filter(adminFilter())
                                .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                                .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                                .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                                .addResponseHeader("X-Response-Source", "product-service") // identifica el microservicio
                                .circuitBreaker(c -> c.setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product-service")
                                        .setRouteId("product-service-fallback")))
                        .uri("lb://product-service"))
                .route("order-service-api-order-admin", r -> r.path("/api/orders/admin/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter())
                                .filter(adminFilter())
                                .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                                .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                                .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                                .addResponseHeader("X-Response-Source", "order-service") // identifica el microservicio
                                .circuitBreaker(c -> c.setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order-service")
                                        .setRouteId("order-service-fallback")))
                        .uri("lb://order-service"))
                .route("order-service-api-order-user", r -> r.path("/api/orders/user/**")
                .filters(f -> f
                        .filter(jwtAuthenticationFilter())
                        .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                        .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                        .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                        .addResponseHeader("X-Response-Source", "order-service") // identifica el microservicio
                        .circuitBreaker(c -> c.setName("orderServiceCircuitBreaker")
                                .setFallbackUri("forward:/fallback/order-service")
                                .setRouteId("order-service-fallback")))
                .uri("lb://order-service"))
                .route("order-service-api-order-item", r -> r.path("/api/orderItems/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter())
                                .addRequestHeader("X-Request-Source", "API-Gateway") // Header para identificar la fuente
                                .addResponseHeader("X-Response-Time", Long.toString(System.currentTimeMillis())) // Header con tiempo de respuesta
                                .addRequestHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis())) // Marca de tiempo de cuando se realizó la solicitud
                                .addResponseHeader("X-Response-Source", "order-service") // identifica el microservicio
                                .circuitBreaker(c -> c.setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order-service")
                                        .setRouteId("order-service-fallback")))
                        .uri("lb://order-service"))
                .route("email-service", r -> r.path("/api/email/**")
                        .uri("lb://email-service"))
                .build();
    }
}
