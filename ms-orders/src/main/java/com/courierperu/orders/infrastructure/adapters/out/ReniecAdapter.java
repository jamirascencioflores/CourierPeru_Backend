package com.courierperu.orders.infrastructure.adapters.out;

import com.courierperu.orders.domain.ports.out.ReniecPort;
import com.courierperu.orders.infrastructure.adapters.out.feign.ReniecFeignClient;
import com.courierperu.orders.infrastructure.adapters.out.feign.dto.ReniecResponse;
import org.springframework.stereotype.Component;

@Component
public class ReniecAdapter implements ReniecPort {

    private final ReniecFeignClient reniecClient;
    private final String TOKEN = "Bearer sk_13499.gX32t4sdiMXWzaNC7DIeQQkzOjXRIZdu";

    public ReniecAdapter(ReniecFeignClient reniecClient) {
        this.reniecClient = reniecClient;
    }

    @Override
    public String obtenerNombreCompleto(String dni) {
        try {
            ReniecResponse res = reniecClient.consultarDni(dni, TOKEN);
            return res.getFullName();
        } catch (Exception e) {
            return "No Encontrado";
        }
    }
}