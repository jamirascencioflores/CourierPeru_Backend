package com.courierperu.orders.application.usecases;

import com.courierperu.orders.domain.model.Order;

import java.util.List;

public interface ManageOrderUseCase {
    Order createOrder(Order order);
    List<Order> getAllOrders();
    Order avanzarEstado(Long id);
    Order findByCodigoRastreo(String codigoRastreo);
    String consultarClientePorDni(String dni);
    List<Order> obtenerOrdenesPorRol(String username, String role);

    Order actualizarEstado(Long id, String estado, String rol);
    void eliminarOrden(Long id, String rol);
}