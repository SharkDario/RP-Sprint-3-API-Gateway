package com.mindhub.api_gateway.controllers;

import com.mindhub.api_gateway.dtos.NewUserDTO;
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

@RestController
@RequestMapping("/user-service")
@Tag(name = "User Service - User Controller", description = "Endpoints for managing users")
public class UserServiceDocumentationUserController {

    //private final String userServiceUrl = "lb://user-service/api/user"; // Load Balancer URL

    @Value("${USER_SERVICE_USERS}") // Environment variable
    private String userServiceUrl; // lb://user-service/api/user -> LoadBalanced in RestTemplate

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Operation(summary = "Verify if a User exists by ID", description = "Return true if user exists, false if not")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/exists/{userId}")
    public Mono<ResponseEntity<Boolean>> existsById(
            @Parameter(description = "User's ID", required = true) @PathVariable Long userId) {
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/exists/" + userId)
                .retrieve()
                .bodyToMono(Boolean.class) // Expect Boolean as response
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Get User's ID by email", description = "Returns User's ID by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/email/{email}")
    public Mono<ResponseEntity<Long>> getByEmail(
            @Parameter(description = "User's email", required = true) @PathVariable String email) {

        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/email/" + email)
                .retrieve()
                .bodyToMono(Long.class) // Expect Long as response
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Get User by email", description = "Returns User's profile by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal error server")
    })
    @GetMapping("/profile/{email}")
    public Mono<ResponseEntity<String>> getProfile(
            @Parameter(description = "User's email", required = true) @PathVariable String email) {
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/profile/" + email)
                .retrieve()
                .bodyToMono(String.class) // Expect Long as response
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
        //return ResponseEntity.ok("User's profile"); // Fictional answer for documentation
    }

    @Operation(summary = "Update User", description = "Updates User's name and email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/profile/{id}")
    public Mono<ResponseEntity<String>> updateProfile(
            @Parameter(description = "User's ID", required = true) @PathVariable Long id,
            @Valid @RequestBody NewUserDTO updateUser) {
        return webClientBuilder.build()
                .patch()
                .uri(userServiceUrl + "/profile/" + id)// Build the URL for the petition PATCH
                .bodyValue(updateUser) // Send the data to update the user
                .retrieve()
                .bodyToMono(String.class) // Expecting the answer in string
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid data provided"));
                    } else if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.internalServerError().body("An unexpected error occurred"));
                });
    }

    @Operation(summary = "Delete User", description = "Deletes a user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> deleteEntityUser(
            @Parameter(description = "User's ID", required = true) @PathVariable Long id) {
        String url = userServiceUrl + "/delete/" + id;

        return webClientBuilder.build()
                .delete()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok("User deleted successfully")) // Mapping answer
                .onErrorResume(error -> {
                    // Manage errors: return NOT_FOUND if fails
                    if (error instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred"));
                });
    }
}
