package com.courierperu.orders.application.usecases;

import com.courierperu.orders.domain.model.Order;

public interface ManageOrderUseCase {
    Order createOrder(Order order);
}