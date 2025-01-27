package com.mindhub.api_gateway.controllers;

import com.mindhub.api_gateway.dtos.NewUserDTO;
import com.mindhub.api_gateway.dtos.RoleType;
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
@RequestMapping("/user-service")
@Tag(name = "User Service - Admin Controller", description = "Endpoints for managing admin-related operations")
public class UserServiceDocumentationAdminController {
    @Value("${USER_SERVICE_ADMINS}") // Environment variable
    private String adminServiceUrl; // lb://admin-service/api/admin -> LoadBalanced in RestTemplate

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Operation(summary = "Get all users", description = "Returns a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/users")
    public Mono<ResponseEntity<List<NewUserDTO>>> getAllUsers() {
        return webClientBuilder.build()
                .get()
                .uri(adminServiceUrl + "/users")
                .retrieve()
                .bodyToFlux(NewUserDTO.class)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Operation(summary = "Get all roles", description = "Returns a list of all available roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/roles")
    public Mono<ResponseEntity<List<RoleType>>> getAllRoles() {
        return webClientBuilder.build()
                .get()
                .uri(adminServiceUrl + "/roles")
                .retrieve()
                .bodyToFlux(RoleType.class)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Operation(summary = "Get user by ID", description = "Returns a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{id}")
    public Mono<ResponseEntity<NewUserDTO>> getUserById(
            @Parameter(description = "User's ID", required = true) @PathVariable Long id) {
        return webClientBuilder.build()
                .get()
                .uri(adminServiceUrl + "/user/" + id)
                .retrieve()
                .bodyToMono(NewUserDTO.class)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/users")
    public Mono<ResponseEntity<String>> createUser(
            @Valid @RequestBody NewUserDTO newUser) {
        return webClientBuilder.build()
                .post()
                .uri(adminServiceUrl + "/users")
                .bodyValue(newUser)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body("User created successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Create a new admin", description = "Creates a new admin user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/admins")
    public Mono<ResponseEntity<String>> createAdmin(
            @Valid @RequestBody NewUserDTO newAdmin) {
        return webClientBuilder.build()
                .post()
                .uri(adminServiceUrl + "/admins")
                .bodyValue(newAdmin)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body("Admin created successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Update user", description = "Updates a user's details by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/user/{id}")
    public Mono<ResponseEntity<String>> updateEntityUser(
            @Parameter(description = "User's ID", required = true) @PathVariable Long id,
            @Valid @RequestBody NewUserDTO updateUser) {
        return webClientBuilder.build()
                .patch()
                .uri(adminServiceUrl + "/user/" + id)
                .bodyValue(updateUser)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("User updated successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    } else if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/user/{id}")
    public Mono<ResponseEntity<String>> deleteEntityUser(
            @Parameter(description = "User's ID", required = true) @PathVariable Long id) {
        return webClientBuilder.build()
                .delete()
                .uri(adminServiceUrl + "/user/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("User deleted successfully"))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }
}
