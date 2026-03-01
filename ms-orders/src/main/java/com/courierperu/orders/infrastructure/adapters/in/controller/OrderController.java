package com.courierperu.orders.infrastructure.adapters.in.controller;

import com.courierperu.orders.application.usecases.ManageOrderUseCase;
import com.courierperu.orders.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ManageOrderUseCase manageOrderUseCase;

    @PostMapping
    public ResponseEntity<Order> create(
            @RequestBody Order order,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        order.setUsuarioUsername(username);
        return new ResponseEntity<>(manageOrderUseCase.createOrder(order), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        // ✨ La lógica de filtrar por rol ya está en tu Service (obtenerOrdenesPorRol)
        return ResponseEntity.ok(manageOrderUseCase.obtenerOrdenesPorRol(username, role));
    }

    @GetMapping("/tracking/{codigo}")
    public ResponseEntity<Order> rastrearPedido(@PathVariable String codigo) {
        return ResponseEntity.ok(manageOrderUseCase.findByCodigoRastreo(codigo));
    }

    @GetMapping("/cliente/{dni}")
    public ResponseEntity<String> consultarCliente(@PathVariable String dni) {
        return ResponseEntity.ok(manageOrderUseCase.consultarClientePorDni(dni));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String rol) {
        try {
            String nuevoEstado = body.getOrDefault("estado", body.values().iterator().next());
            return ResponseEntity.ok(manageOrderUseCase.actualizarEstado(id, nuevoEstado, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: " + e.getMessage());
        }
    }

    // ✨ MODIFICADO: Lee el rol desde el Header
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String rol) {
        try {
            manageOrderUseCase.eliminarOrden(id, rol);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long id,
            @RequestBody Order orderRequest,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String rol) {
        try {
            return ResponseEntity.ok(manageOrderUseCase.editarOrden(id, orderRequest, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error al editar: " + e.getMessage());
        }
    }
}