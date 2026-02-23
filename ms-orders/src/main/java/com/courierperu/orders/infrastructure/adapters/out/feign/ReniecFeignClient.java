package com.courierperu.orders.infrastructure.adapters.out.feign;

import com.courierperu.orders.infrastructure.adapters.out.feign.dto.ReniecResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reniec-api", url = "https://api.decolecta.com/v1/reniec")
public interface ReniecFeignClient {

    @GetMapping("/dni")
    ReniecResponse consultarDni(@RequestParam("numero") String numero,
                                @RequestHeader("Authorization") String token);
}