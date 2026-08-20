package com.pizzeria.app.order.repository;

import com.pizzeria.app.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

// No hay una capa de servicio para el modulo de Pedidos: el controlador
// inyecta y usa este repositorio (y el de OrderItem) directamente.
public interface OrderRepository extends JpaRepository<Order, Long> {
}
