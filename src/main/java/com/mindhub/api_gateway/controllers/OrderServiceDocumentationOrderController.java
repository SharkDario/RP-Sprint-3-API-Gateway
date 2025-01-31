package com.mindhub.api_gateway.controllers;

import com.mindhub.api_gateway.dtos.NewOrderRecord;
import com.mindhub.api_gateway.dtos.OrderCreatedRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/order-service")
@Tag(name = "Order Service - Order Controller", description = "Endpoints for managing orders")
public class OrderServiceDocumentationOrderController {

    @Value("${ORDER_SERVICE_ORDERS}") // Environment variable
    private String orderServiceUrl; // lb://order-service/api/orders -> LoadBalanced in RestTemplate

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public Mono<ResponseEntity<OrderCreatedRecord>> createOrder(
            @Valid @RequestBody NewOrderRecord newOrderDTO) {
        return webClientBuilder.build()
                .post()
                .uri(orderServiceUrl)
                .bodyValue(newOrderDTO)
                .retrieve()
                .bodyToMono(OrderCreatedRecord.class) // Expect OrderCreatedRecord as response
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Get all orders", description = "Returns a list of all orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Mono<ResponseEntity<List<NewOrderRecord>>> getAllOrders() {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl)
                .retrieve()
                .bodyToFlux(NewOrderRecord.class)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Operation(summary = "Update an order", description = "Updates an order's status by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}")
    public Mono<ResponseEntity<String>> updateOrder(
            @Parameter(description = "Order's ID", required = true) @PathVariable Long id,
            @Valid @RequestBody NewOrderRecord updateOrderDTO) {
        return webClientBuilder.build()
                .patch()
                .uri(orderServiceUrl + "/" + id)
                .bodyValue(updateOrderDTO)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("Order updated successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    } else if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Delete an order", description = "Deletes an order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteOrder(
            @Parameter(description = "Order's ID", required = true) @PathVariable Long id) {
        return webClientBuilder.build()
                .delete()
                .uri(orderServiceUrl + "/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("Order deleted successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }
}