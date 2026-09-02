package com.pizzeria.app.order.repository;

import com.pizzeria.app.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);
}
