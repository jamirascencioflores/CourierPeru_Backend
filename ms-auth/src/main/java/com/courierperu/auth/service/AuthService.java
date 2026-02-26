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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return repository.save(user);
    }

    // 2. Buscamos el usuario en la BD para sacar su rol real
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