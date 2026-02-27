package com.courierperu.orders.application.service;

import com.courierperu.orders.application.usecases.ManageOrderUseCase;
import com.courierperu.orders.domain.model.Order;
import com.courierperu.orders.domain.ports.out.OrderRepositoryPort;
import com.courierperu.orders.domain.ports.out.ReniecPort;
import com.courierperu.orders.infrastructure.adapters.out.feign.ShippingFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import correcto
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements ManageOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ShippingFeignClient shippingFeignClient;
    private final RabbitTemplate rabbitTemplate;
    private final ReniecPort reniecPort; // Inyéctalo en el constructor


    @Override
    public Order createOrder(Order order) {
        log.info("Procesando pedido para: {}", order.getDniCliente());

        // --- HILO 1: Validación de Cliente (Local/Simulado) ---
        CompletableFuture<Boolean> validacionCliente = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(800);
                log.info("✅ Cliente validado correctamente.");
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        });

        // --- HILO 2: Consulta a MS-SHIPPING (Remoto vía Feign) ---
        CompletableFuture<Double> calculoEnvio = CompletableFuture.supplyAsync(() -> {
            log.info("🚚 Consultando costo a MS-SHIPPING...");
            var rate = shippingFeignClient.calcular(order.getPesoPaquete());
            log.info("💰 Costo recibido: S/ {}", rate.costo());
            return rate.costo();
        });

        // --- COMPOSICIÓN PARALELA (Esperar a ambos) ---
        CompletableFuture.allOf(validacionCliente, calculoEnvio).join();

        // --- UNIFICACIÓN DE RESULTADOS ---
        try {
            if (validacionCliente.get()) {
                // Solo asignamos el valor, NO guardamos todavía
                order.setCostoEnvio(calculoEnvio.get());
            } else {
                throw new RuntimeException("Cliente no válido o verificación fallida");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error en composición de servicios", e);
        }

        // --- PERSISTENCIA (Guardamos 1 sola vez) ---
        Order orderGuardada = orderRepositoryPort.save(order);
        log.info("💾 Pedido guardado en BD con ID: {}", orderGuardada.getId());

        // --- MENSAJERÍA ASÍNCRONA (RabbitMQ) ---
        try {
            String mensaje = "Pedido creado con éxito. Tracking: " + orderGuardada.getCodigoRastreo();
            // Enviamos a la cola "cola.correos"
            rabbitTemplate.convertAndSend("cola.correos", mensaje);
            log.info("📧 Mensaje enviado a RabbitMQ: {}", mensaje);
        } catch (Exception e) {
            // OJO: Si falla RabbitMQ, NO debemos cancelar el pedido, solo loguear el error.
            log.error("❌ Error al enviar mensaje a RabbitMQ", e);
        }

        return orderGuardada;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepositoryPort.findAll();
    }

    @Override
    public Order avanzarEstado(Long id) {
        Order order = orderRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if ("PENDIENTE".equals(order.getEstado()) || "PENDIENTE_RECOJO".equals(order.getEstado())) {
            order.setEstado("EN_RUTA");

            // ✨ NUEVO: Notificamos que ya está en camino
            try {
                String mensaje = "¡Hola! Tu paquete con rastreo " + order.getCodigoRastreo() + " ya está EN RUTA hacia su destino.";
                rabbitTemplate.convertAndSend("cola.correos", mensaje);
                log.info("📧 Notificación de EN_RUTA enviada a RabbitMQ");
            } catch (Exception e) {
                log.error("❌ Error al notificar en_ruta", e);
            }

        } else if ("EN_RUTA".equals(order.getEstado())) {
            order.setEstado("ENTREGADO");

            // ✨ ESTE YA LO TENÍAS: Notificamos que se entregó
            try {
                String mensaje = "¡Hola! Tu paquete con rastreo " + order.getCodigoRastreo() + " ha sido ENTREGADO con éxito.";
                rabbitTemplate.convertAndSend("cola.correos", mensaje);
                log.info("📧 Notificación de entrega enviada a RabbitMQ");
            } catch (Exception e) {
                log.error("❌ Error al notificar entrega", e);
            }
        }

        return orderRepositoryPort.save(order);
    }
    public Order findByCodigoRastreo(String codigoRastreo) {
        return orderRepositoryPort.findByCodigoRastreo(codigoRastreo)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
    }

    @Override
    public String consultarClientePorDni(String dni) {
        return reniecPort.obtenerNombreCompleto(dni);
    }

    @Override
    public List<Order> obtenerOrdenesPorRol(String username, String role) {
        // Si es administrador, le damos el historial completo
        if ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
            return orderRepositoryPort.findAll();
        }
        // Si es cliente normal, solo le damos sus propias órdenes
        return orderRepositoryPort.findByUsuarioUsername(username);
    }

    @Override
    public void eliminarOrden(Long id, String rol) {
        Order order = orderRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        // Validar regla de negocio
        if (!rol.contains("ADMIN") && !"PENDIENTE".equalsIgnoreCase(order.getEstado())) {
            throw new RuntimeException("Solo puedes cancelar órdenes en estado PENDIENTE.");
        }

        orderRepositoryPort.deleteById(id);
    }

    @Override
    public Order actualizarEstado(Long id, String nuevoEstado, String rol) {
        Order order = orderRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (!rol.contains("ADMIN") && !"PENDIENTE".equalsIgnoreCase(order.getEstado())) {
            throw new RuntimeException("No puedes editar una orden que ya está en proceso.");
        }

        order.setEstado(nuevoEstado);
        return orderRepositoryPort.save(order);
    }

    @Override
    public Order editarOrden(Long id, Order ordenActualizada, String rol) {
        Order order = orderRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        boolean isAdmin = rol != null && rol.contains("ADMIN");

        // Regla 1: Validar el estado
        if (!isAdmin && !"PENDIENTE".equalsIgnoreCase(order.getEstado())) {
            throw new RuntimeException("Solo puedes editar órdenes en estado PENDIENTE.");
        }

        // Regla 2: Actualizar campos básicos permitidos
        order.setDireccionEntrega(ordenActualizada.getDireccionEntrega());
        order.setDireccionRecojo(ordenActualizada.getDireccionRecojo());

        // Regla 3: Si es ADMIN y el peso cambió, recalcular con MS-SHIPPING
        if (isAdmin && ordenActualizada.getPesoPaquete() != null
                && !ordenActualizada.getPesoPaquete().equals(order.getPesoPaquete())) {

            order.setPesoPaquete(ordenActualizada.getPesoPaquete());
            log.info("🚚 Recalculando costo por cambio de peso a: {} kg", order.getPesoPaquete());

            try {
                var rate = shippingFeignClient.calcular(order.getPesoPaquete());
                order.setCostoEnvio(rate.costo());
                log.info("💰 Nuevo costo asignado: S/ {}", rate.costo());
            } catch (Exception e) {
                log.error("❌ Error al recalcular costo con MS-SHIPPING", e);
                throw new RuntimeException("No se pudo recalcular el costo de envío.");
            }
        }
        return orderRepositoryPort.save(order);
    }
}