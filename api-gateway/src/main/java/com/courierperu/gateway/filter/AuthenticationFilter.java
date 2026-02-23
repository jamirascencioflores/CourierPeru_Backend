package com.courierperu.gateway.filter;

import com.courierperu.gateway.config.JwtUtil;
import com.courierperu.gateway.config.RouteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

                // 1. Verificar si existe el header de Authorization
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Falta el encabezado de autorización", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // 1. Validar el Token
                    jwtUtil.validateToken(authHeader);

                    // 2. Extraer el rol y el usuario (NUEVO)
                    String role = jwtUtil.extractRole(authHeader);
                    String username = jwtUtil.extractUsername(authHeader); // ✨ Extraemos el usuario

                    // 3. Mutamos la petición para inyectar AMBOS headers ocultos
                    org.springframework.http.server.reactive.ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .header("X-User-Role", role)
                            .header("X-User-Name", username) // ✨ Inyectamos el usuario
                            .build();

                    // Pasamos la petición modificada al siguiente nivel
                    return chain.filter(exchange.mutate().request(request).build());

                } catch (Exception e) {
                    return onError(exchange, "Token no válido o expirado", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        });
    }

    // Método de apoyo para enviar la respuesta de error controlada
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config { }
}