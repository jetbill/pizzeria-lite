package com.pizzeria.app.order.repository;

import com.pizzeria.app.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Long> {
}
