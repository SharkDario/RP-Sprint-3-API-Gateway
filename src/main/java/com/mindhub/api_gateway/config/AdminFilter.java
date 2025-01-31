package com.mindhub.api_gateway.config;

import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component
public class AdminFilter implements GatewayFilter {
    @Autowired
    private JwtUtils jwtUtil;

    public AdminFilter(JwtUtils jwtUtils){
        this.jwtUtil = jwtUtils;
    }
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        token = token.substring(7);
        String role = jwtUtil.extractRole(token);
        if (role.equals("ADMIN")){
            return chain.filter(exchange);
        }else{
            return onError(exchange, "The user doesn't have permission", HttpStatus.UNAUTHORIZED);
        }
    }
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }
}


/*
private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    exchange.getResponse().setStatusCode(httpStatus);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    String errorMessage = "{"error": "" + err + ""}";
    DataBuffer buffer = exchange.getResponse()
            .bufferFactory()
            .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

    return exchange.getResponse().writeWith(Mono.just(buffer));
}
 */