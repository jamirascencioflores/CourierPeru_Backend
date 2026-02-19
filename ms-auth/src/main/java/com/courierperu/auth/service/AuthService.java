package com.courierperu.auth.service;

import com.courierperu.auth.entity.AuthUser;
import com.courierperu.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Guardar usuario encriptado
    public AuthUser saveUser(AuthUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        return repository.save(user);
    }

    // Generar token si el usuario es válido
    public String generateToken(String username) {
        return jwtService.createToken(username);
    }

    // (Opcional) Validar credenciales manualmente si no usamos AuthenticationManager directo
}