package com.courierperu.orders.infrastructure.repository;

import com.courierperu.orders.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCodigoRastreo(String codigoRastreo);
    List<Order> findByUsuarioUsername(String usuarioUsername);
}