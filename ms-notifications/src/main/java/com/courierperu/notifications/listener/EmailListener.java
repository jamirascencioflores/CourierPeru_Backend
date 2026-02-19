package com.courierperu.notifications.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailListener {

    @RabbitListener(queues = "cola.correos")
    public void recibirMensaje(String mensaje) {
        log.info("📨 Notificación recibida: {}", mensaje);

        // Aquí iría la lógica real de JavaMailSender
        simularEnvioCorreo();
    }

    private void simularEnvioCorreo() {
        try {
            log.info("Generando PDF de la guía...");
            Thread.sleep(2000); // Simulamos proceso pesado
            log.info("✅ Correo enviado al cliente.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}