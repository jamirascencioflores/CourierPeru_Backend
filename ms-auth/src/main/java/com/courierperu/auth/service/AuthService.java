package com.courierperu.auth.service;

import com.courierperu.auth.entity.AuthUser;
import com.courierperu.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthUser saveUser(AuthUser user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // ✨ Validar que el DNI no esté duplicado (si el usuario lo envía)
        if (user.getDni() != null && repository.existsByDni(user.getDni())) {
            throw new RuntimeException("Este DNI ya se encuentra registrado en el sistema");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return repository.save(user);
    }

    public String generateToken(String username) {
        AuthUser user = repository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return jwtService.createToken(username, user.getRole());
    }

    public AuthUser getUserDetails(String username) {
        return repository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}