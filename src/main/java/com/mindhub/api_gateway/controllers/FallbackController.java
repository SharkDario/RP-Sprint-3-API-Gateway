package com.mindhub.api_gateway.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    private FallbackController() {
    }

    public static FallbackController createFallbackController() {
        return new FallbackController();
    }

    @GetMapping("/fallback/user-service")
    public ResponseEntity<String> userServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("User Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/fallback/product-service")
    public ResponseEntity<String> productServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Product Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/fallback/order-service")
    public ResponseEntity<String> orderServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Order Service is currently unavailable. Please try again later.");
    }
}