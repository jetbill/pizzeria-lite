package com.pizzeria.app.order.controller;

import com.pizzeria.app.order.dto.OrderRequest;
import com.pizzeria.app.order.dto.OrderResponse;
import com.pizzeria.app.order.dto.OrderStatusUpdateRequest;
import com.pizzeria.app.order.entity.Order;
import com.pizzeria.app.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.findAll().stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponse.from(orderService.findById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrdersByCustomer(@RequestParam String customerName) {
        List<OrderResponse> results = orderService.search(customerName).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Order saved = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(saved));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                            @Valid @RequestBody OrderStatusUpdateRequest request) {
        Order updated = orderService.updateStatus(id, request.status());
        return ResponseEntity.ok(OrderResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
