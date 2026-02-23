package com.courierperu.orders.infrastructure.adapters.in.controller;

import com.courierperu.orders.application.usecases.ManageOrderUseCase;
import com.courierperu.orders.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Importar List

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor

public class OrderController {

    private final ManageOrderUseCase manageOrderUseCase;

    @PostMapping
    public ResponseEntity<Order> create(
            @RequestBody Order order,
            @RequestHeader(value = "X-User-Name", required = false) String username) { // ✨ Leemos quién la crea

        order.setUsuarioUsername(username); // Asignamos el dueño de forma segura en el backend
        return new ResponseEntity<>(manageOrderUseCase.createOrder(order), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Name", required = false) String username) { // ✨ Leemos rol y usuario

        return ResponseEntity.ok(manageOrderUseCase.obtenerOrdenesPorRol(username, role));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> avanzarEstado(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) { // ✨ Leemos el header

        if (!"ADMIN".equals(role) && !"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Esta acción es exclusiva para administradores.");
        }

        Order orderActualizada = manageOrderUseCase.avanzarEstado(id);
        return ResponseEntity.ok(orderActualizada);
    }

    @GetMapping("/tracking/{codigo}")
    public ResponseEntity<Order> rastrearPedido(@PathVariable String codigo) {
        return ResponseEntity.ok(manageOrderUseCase.findByCodigoRastreo(codigo));
    }

    @GetMapping("/cliente/{dni}")
    public ResponseEntity<String> consultarCliente(@PathVariable String dni) {
        return ResponseEntity.ok(manageOrderUseCase.consultarClientePorDni(dni));
    }

}