package com.courierperu.orders.application.service;

import com.courierperu.orders.application.usecases.ManageOrderUseCase;
import com.courierperu.orders.domain.model.Order;
import com.courierperu.orders.domain.ports.out.OrderRepositoryPort;
import com.courierperu.orders.infrastructure.adapters.out.feign.ShippingFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import correcto
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements ManageOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ShippingFeignClient shippingFeignClient;
    private final RabbitTemplate rabbitTemplate;

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
}