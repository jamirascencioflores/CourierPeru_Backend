package com.courierperu.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alias;      // Ej: "Casa", "Oficina", "Almacén"
    private String calle;      // Ej: "Av. Los Pinos 123"
    private String departamento;
    private String provincia;
    private String distrito;   // Ej: "Miraflores"
    private String referencia; // Ej: "Frente al parque"

    // Relación: Muchas direcciones pertenecen a un Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // Evita errores de bucle infinito al devolver el JSON
    private AuthUser user;
}