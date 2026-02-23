package com.courierperu.orders.domain.ports.out;

import com.courierperu.orders.domain.model.Order;
import java.util.Optional;
import java.util.List;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    Optional<Order> findByCodigoRastreo(String codigoRastreo);
    List<Order> findByUsuarioUsername(String username);
}