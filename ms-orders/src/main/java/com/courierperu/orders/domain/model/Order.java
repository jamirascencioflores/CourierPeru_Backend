package com.courierperu.orders.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoRastreo; // Ej: CP-2024-001
    private String dniCliente;
    private String direccionRecojo;
    private String direccionEntrega;
    private Double pesoPaquete; // En Kg
    private String estado; // PENDIENTE, EN_RUTA, ENTREGADO
    private Double costoEnvio;

    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
        this.codigoRastreo = "CP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}