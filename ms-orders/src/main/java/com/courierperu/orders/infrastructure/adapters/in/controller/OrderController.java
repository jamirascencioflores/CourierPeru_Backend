package com.courierperu.orders.infrastructure.adapters.in.controller;

import com.courierperu.orders.application.usecases.ManageOrderUseCase;
import com.courierperu.orders.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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

    @GetMapping("/tracking/{codigo}")
    public ResponseEntity<Order> rastrearPedido(@PathVariable String codigo) {
        return ResponseEntity.ok(manageOrderUseCase.findByCodigoRastreo(codigo));
    }

    @GetMapping("/cliente/{dni}")
    public ResponseEntity<String> consultarCliente(@PathVariable String dni) {
        return ResponseEntity.ok(manageOrderUseCase.consultarClientePorDni(dni));
    }

    // PUT: Para EDITAR (Cambiar a EN_RUTA, ENTREGADO, etc.)
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Long id, @RequestBody String estado, Authentication auth) {
        try {
            String rol = auth.getAuthorities().iterator().next().getAuthority();
            return ResponseEntity.ok(manageOrderUseCase.actualizarEstado(id, estado, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: " + e.getMessage());
        }
    }

    // DELETE: Para ELIMINAR de la base de datos
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id, Authentication auth) {
        try {
            String rol = auth.getAuthorities().iterator().next().getAuthority();
            manageOrderUseCase.eliminarOrden(id, rol);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: " + e.getMessage());
        }
    }

}