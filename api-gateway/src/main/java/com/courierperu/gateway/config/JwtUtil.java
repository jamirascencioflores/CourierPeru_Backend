package com.courierperu.gateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    // DEBE SER LA MISMA QUE EN MS-AUTH
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey()) // Tu método actual que obtiene la llave
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class); // Asegúrate de que "role" sea el nombre correcto en tu JWT
    }
    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody().getSubject();
    }
}