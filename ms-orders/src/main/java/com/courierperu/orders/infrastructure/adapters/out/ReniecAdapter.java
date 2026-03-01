package com.courierperu.orders.infrastructure.adapters.out;

import com.courierperu.orders.domain.ports.out.ReniecPort;
import com.courierperu.orders.infrastructure.adapters.out.feign.ReniecFeignClient;
import com.courierperu.orders.infrastructure.adapters.out.feign.dto.ReniecResponse;
import org.springframework.stereotype.Component;
import feign.FeignException;

@Component
public class ReniecAdapter implements ReniecPort {

    private final ReniecFeignClient reniecClient;
    private final String TOKEN = "Bearer sk_13499.b1tVCjVhmS0H9QBR0baiwW17DOEWTeiq";

    public ReniecAdapter(ReniecFeignClient reniecClient) {
        this.reniecClient = reniecClient;
    }

    @Override
    public String obtenerNombreCompleto(String dni) {
        try {
            ReniecResponse res = reniecClient.consultarDni(dni, TOKEN);
            return res.getFullName();

        } catch (FeignException.NotFound e) {
            // Atrapa el error 404 exacto de Decolecta
            System.out.println("El DNI " + dni + " no existe en la base de datos del proveedor.");
            return "Cliente no registrado en RENIEC";

        } catch (FeignException.Unauthorized e) {
            System.out.println("Error 401: El Token de la API caducó.");
            return "Error de validación interna";

        } catch (Exception e) {
            System.out.println("Error de conexión con la API externa: " + e.getMessage());
            return "No Encontrado";
        }
    }
}