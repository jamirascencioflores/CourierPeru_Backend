package com.courierperu.shipping.infrastructure.controller;

import com.courierperu.shipping.domain.model.ShippingRate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    @GetMapping("/calcular")
    public ResponseEntity<ShippingRate> calcular(@RequestParam Double peso) {

        // Simulación de lógica de negocio compleja
        Double tarifaBase = 10.0;
        Double costoFinal = tarifaBase + (peso * 2.5);

        ShippingRate rate = ShippingRate.builder()
                .costo(costoFinal)
                .tipoServicio("STANDARD")
                .fechaEstimada(LocalDate.now().plusDays(2)) // Llega en 2 días
                .build();

        // Simulamos un pequeño delay para que se note el paralelismo en el otro servicio
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        return ResponseEntity.ok(rate);
    }
}