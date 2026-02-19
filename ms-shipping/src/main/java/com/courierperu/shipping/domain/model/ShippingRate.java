package com.courierperu.shipping.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ShippingRate {
    private Double costo;
    private String tipoServicio; // EXPRESS, STANDARD
    private LocalDate fechaEstimada;
}