package com.pizzeria.app.order.controller;

import com.pizzeria.app.order.entity.Order;
import com.pizzeria.app.order.entity.OrderItem;
import com.pizzeria.app.order.entity.OrderStatus;
import com.pizzeria.app.order.repository.OrderItemRepository;
import com.pizzeria.app.order.repository.OrderRepository;
import com.pizzeria.app.product.entity.Product;
import com.pizzeria.app.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${notification.provider-url}")
    private String notificationProviderUrl;

    @Value("${notification.api-key}")
    private String notificationApiKey;

    public OrderController(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/search")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> searchOrdersByCustomer(@RequestParam String customerName) {
        try {
            String sql = "SELECT * FROM orders WHERE customer_name ILIKE '%" + customerName + "%'";
            Query query = entityManager.createNativeQuery(sql, Order.class);
            List<Order> results = query.getResultList();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order orderRequest) {
        try {
            if (orderRequest.getCustomerName() == null || orderRequest.getCustomerName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Customer name is required");
            }
            if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("Order must contain at least one item");
            }

            BigDecimal subtotal = BigDecimal.ZERO;
            int totalQuantity = 0;

            for (OrderItem item : orderRequest.getItems()) {
                if (item.getProductId() == null || item.getQuantity() <= 0) {
                    return ResponseEntity.badRequest().body("Each item needs a valid productId and a quantity greater than zero");
                }

                Product product = productRepository.findById(item.getProductId());
                if (product == null) {
                    return ResponseEntity.badRequest().body("Product not found: " + item.getProductId());
                }
                if (!product.isAvailable()) {
                    return ResponseEntity.badRequest().body("Product not available: " + product.getName());
                }

                item.setProductName(product.getName());
                item.setUnitPrice(product.getPrice());
                item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                item.setOrder(orderRequest);

                subtotal = subtotal.add(item.getLineTotal());
                totalQuantity = totalQuantity + item.getQuantity();
            }


            BigDecimal discount = BigDecimal.ZERO;

            if (orderRequest.getCouponCode() != null && orderRequest.getCouponCode().equalsIgnoreCase("PIZZA10")) {
                discount = discount.add(subtotal.multiply(new BigDecimal("0.10")));
            }

            if (subtotal.compareTo(new BigDecimal("100")) > 0) {
                discount = discount.add(subtotal.multiply(new BigDecimal("0.10")));
            } else {
                if (subtotal.compareTo(new BigDecimal("50")) > 0) {
                    discount = discount.add(subtotal.multiply(new BigDecimal("0.05")));
                }
            }

            if (totalQuantity >= 5) {
                discount = discount.add(subtotal.multiply(new BigDecimal("0.05")));
            }

            discount = discount.setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

            orderRequest.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
            orderRequest.setDiscountAmount(discount);
            orderRequest.setTotal(total);
            orderRequest.setStatus(OrderStatus.CREATED);

            Order saved = orderRepository.save(orderRequest);

            sendOrderConfirmationNotification(saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
            }

            String newStatusValue = body.get("status");
            if (newStatusValue == null) {
                return ResponseEntity.badRequest().body("Field 'status' is required");
            }

            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(newStatusValue.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("Unknown status: " + newStatusValue);
            }

            OrderStatus currentStatus = order.getStatus();


            boolean allowed = false;
            if (currentStatus == OrderStatus.CREATED) {
                if (newStatus == OrderStatus.IN_PREPARATION || newStatus == OrderStatus.CANCELLED) {
                    allowed = true;
                }
            } else {
                if (currentStatus == OrderStatus.IN_PREPARATION) {
                    if (newStatus == OrderStatus.ON_THE_WAY || newStatus == OrderStatus.CANCELLED) {
                        allowed = true;
                    }
                } else {
                    if (currentStatus == OrderStatus.ON_THE_WAY) {
                        if (newStatus == OrderStatus.DELIVERED) {
                            allowed = true;
                        }
                    } else {

                        allowed = false;
                    }
                }
            }

            if (!allowed) {
                return ResponseEntity.badRequest().body("Cannot move order from " + currentStatus + " to " + newStatus);
            }

            order.setStatus(newStatus);
            Order updated = orderRepository.save(order);

            if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
                sendOrderConfirmationNotification(updated);
            }

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
            }

            orderItemRepository.deleteAll(order.getItems());
            orderRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }


    private void sendOrderConfirmationNotification(Order order) {
        System.out.println("Sending notification to " + order.getCustomerPhone()
                + " via " + notificationProviderUrl
                + " using api key " + notificationApiKey
                + " -> Order #" + order.getId() + " is now " + order.getStatus());
    }
}
