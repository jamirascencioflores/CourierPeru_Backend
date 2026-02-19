package com.courierperu.orders.infrastructure.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-shipping")
public interface ShippingFeignClient {

    record ShippingRateDto(Double costo, String fechaEstimada, String tipoServicio) {}

    @GetMapping("/api/shipping/calculate")
    ShippingRateDto calcular(@RequestParam("peso") Double peso);
}