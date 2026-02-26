package com.courierperu.auth.controller;

import com.courierperu.auth.dto.AuthRequest;
import com.courierperu.auth.entity.AuthUser;
import com.courierperu.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String addNewUser(@RequestBody AuthUser user) {
        service.saveUser(user);
        return "Usuario registrado con éxito";
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<?> getToken(@RequestBody AuthRequest authRequest) { // Cambiamos a ResponseEntity<?>
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );

            if (authenticate.isAuthenticated()) {
                String token = service.generateToken(authRequest.getUserName());

                // ✨ CREAMOS UN MAPA PARA QUE SPRING LO CONVIERTA A JSON {"token": "ey..."}
                Map<String, String> response = new HashMap<>();
                response.put("token", token);

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(401).body(Map.of("error", "Usuario no autenticado"));
        } catch (Exception e) {
            System.out.println("ERROR EN LOGIN: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas: " + e.getMessage()));
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<AuthUser> getProfile(
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        // ✨ RADAR MS-AUTH
        System.out.println("📨 MS-AUTH: Petición a /perfil recibida. Header X-User-Name: " + username);
        if (username == null) {
            System.out.println("❌ MS-AUTH: Bloqueando petición porque el header es nulo.");
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(service.getUserDetails(username));
    }
}