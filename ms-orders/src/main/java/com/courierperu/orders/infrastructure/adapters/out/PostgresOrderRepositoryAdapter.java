package com.courierperu.orders.infrastructure.adapters.out;

import com.courierperu.orders.domain.model.Order;
import com.courierperu.orders.domain.ports.out.OrderRepositoryPort;
import com.courierperu.orders.infrastructure.repository.JpaOrderRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class PostgresOrderRepositoryAdapter implements OrderRepositoryPort {

    private final JpaOrderRepository jpaRepository;

    public PostgresOrderRepositoryAdapter(JpaOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll();
    }
}