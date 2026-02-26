package com.courierperu.gateway.filter;

import com.courierperu.gateway.config.JwtUtil;
import com.courierperu.gateway.config.RouteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {

                // 1. Verificar si existe el header
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Falta el encabezado de autorización", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // 2. Validar Token y extraer datos
                    jwtUtil.validateToken(authHeader);
                    String role = jwtUtil.extractRole(authHeader);
                    String username = jwtUtil.extractUsername(authHeader);

                    // 3. Inyectar los headers ocultos
                    ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .header("X-User-Role", role)
                            .header("X-User-Name", username)
                            .build();

                    return chain.filter(exchange.mutate().request(request).build());

                } catch (Exception e) {
                    // ✨ RADAR GATEWAY
                    System.out.println("❌ ERROR EN GATEWAY (Filtro): " + e.getMessage());
                    return onError(exchange, "Token no válido o expirado", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config { }
}