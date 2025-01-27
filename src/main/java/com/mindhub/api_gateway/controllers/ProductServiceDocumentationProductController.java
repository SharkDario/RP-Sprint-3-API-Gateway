package com.mindhub.api_gateway.controllers;

import com.mindhub.api_gateway.dtos.ExistentProductsRecord;
import com.mindhub.api_gateway.dtos.NewProductDTO;
import com.mindhub.api_gateway.dtos.ProductQuantityRecord;
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
@RequestMapping("/product-service")
@Tag(name = "Product Service - Product Controller", description = "Endpoints for managing products")
public class ProductServiceDocumentationProductController {

    @Value("${PRODUCT_SERVICE}") // Environment variable
    private String productServiceUrl; // lb://product-service/api/products -> LoadBalanced in RestTemplate

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Operation(summary = "Check if a product exists by ID", description = "Returns true if the product exists, false otherwise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product existence verified"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/exists/{productId}")
    public Mono<ResponseEntity<Boolean>> existsById(
            @Parameter(description = "Product's ID", required = true) @PathVariable Long productId) {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/exists/" + productId)
                .retrieve()
                .bodyToMono(Boolean.class) // Expect Boolean as response
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Operation(summary = "Get all products", description = "Returns a list of all products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Mono<ResponseEntity<List<NewProductDTO>>> getAllProducts() {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl)
                .retrieve()
                .bodyToFlux(NewProductDTO.class)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public Mono<ResponseEntity<String>> createProduct(
            @Valid @RequestBody NewProductDTO newProductDTO) {
        return webClientBuilder.build()
                .post()
                .uri(productServiceUrl)
                .bodyValue(newProductDTO)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body("Product created successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Update a product", description = "Updates a product's details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}")
    public Mono<ResponseEntity<String>> updateProduct(
            @Parameter(description = "Product's ID", required = true) @PathVariable Long id,
            @Valid @RequestBody NewProductDTO updateProductDTO) {
        return webClientBuilder.build()
                .patch()
                .uri(productServiceUrl + "/" + id)
                .bodyValue(updateProductDTO)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("Product updated successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    } else if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteProduct(
            @Parameter(description = "Product's ID", required = true) @PathVariable Long id) {
        return webClientBuilder.build()
                .delete()
                .uri(productServiceUrl + "/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("Product deleted successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Check product availability", description = "Returns a list of available products based on the provided list of product quantities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product availability checked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping
    public Mono<ResponseEntity<List<ExistentProductsRecord>>> existsProducts(
            @RequestBody List<ProductQuantityRecord> recordList) {
        return webClientBuilder.build()
                .put()
                .uri(productServiceUrl)
                .bodyValue(recordList)
                .retrieve()
                .bodyToFlux(ExistentProductsRecord.class)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}