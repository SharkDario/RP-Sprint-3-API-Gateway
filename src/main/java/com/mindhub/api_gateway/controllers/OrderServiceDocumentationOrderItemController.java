package com.mindhub.api_gateway.controllers;
import com.mindhub.api_gateway.dtos.NewOrderItemDTO;
import com.mindhub.api_gateway.dtos.NewOrderItemRecord;
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
@RequestMapping("/order-item-service")
@Tag(name = "Order Item Service - Order Item Controller", description = "Endpoints for managing order items")
public class OrderServiceDocumentationOrderItemController {

    @Value("${ORDER_SERVICE_ORDER_ITEMS}") // Environment variable
    private String orderItemServiceUrl; // lb://order-item-service/api/orderItems -> LoadBalanced in RestTemplate

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Operation(summary = "Create an order item", description = "Creates a new order item for a specific order and product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "404", description = "Order or product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{orderId}/{productId}")
    public Mono<ResponseEntity<String>> createOrderItem(
            @Parameter(description = "Order's ID", required = true) @PathVariable Long orderId,
            @Parameter(description = "Product's ID", required = true) @PathVariable Long productId,
            @Valid @RequestBody NewOrderItemDTO newOrderItemDTO) {
        return webClientBuilder.build()
                .post()
                .uri(orderItemServiceUrl + "/" + orderId + "/" + productId)
                .bodyValue(newOrderItemDTO)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body("Order Item created successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    } else if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Get all order items", description = "Returns a list of all order items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order items retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Mono<ResponseEntity<List<NewOrderItemRecord>>> getAllOrderItems() {
        return webClientBuilder.build()
                .get()
                .uri(orderItemServiceUrl)
                .retrieve()
                .bodyToFlux(NewOrderItemRecord.class)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Operation(summary = "Update an order item", description = "Updates an order item's details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "404", description = "Order item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}")
    public Mono<ResponseEntity<String>> updateOrderItem(
            @Parameter(description = "Order item's ID", required = true) @PathVariable Long id,
            @Valid @RequestBody NewOrderItemDTO updateOrderItemDTO) {
        return webClientBuilder.build()
                .patch()
                .uri(orderItemServiceUrl + "/" + id)
                .bodyValue(updateOrderItemDTO)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("Order Item updated successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    } else if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Delete an order item", description = "Deletes an order item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteOrderItem(
            @Parameter(description = "Order item's ID", required = true) @PathVariable Long id) {
        return webClientBuilder.build()
                .delete()
                .uri(orderItemServiceUrl + "/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("Order Item deleted successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }
}
