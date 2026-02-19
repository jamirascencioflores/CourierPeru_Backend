package com.courierperu.auth.controller;

import com.courierperu.auth.dto.AuthRequest;
import com.courierperu.auth.entity.AuthUser;
import com.courierperu.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public String getToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );

            if (authenticate.isAuthenticated()) {
                return service.generateToken(authRequest.getUserName());
            }
            return "No autenticado";
        } catch (Exception e) {
            // Esto imprimirá el error real en la consola de IntelliJ
            System.out.println("ERROR EN LOGIN: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}