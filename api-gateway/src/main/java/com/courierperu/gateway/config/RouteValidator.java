package com.courierperu.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/eureka",
            "/api/orders/cliente"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                // ✨ LA MAGIA: Dejamos pasar las peticiones OPTIONS del navegador libres
                if (request.getMethod().name().equals("OPTIONS")) {
                    return false;
                }

                // Para el resto (GET, POST, etc), aplicamos tu regla normal
                return openApiEndpoints
                        .stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));
            };
}