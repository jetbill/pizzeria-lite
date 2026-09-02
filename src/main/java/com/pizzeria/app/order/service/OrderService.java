package com.pizzeria.app.order.service;

import com.pizzeria.app.common.exception.BusinessValidationException;
import com.pizzeria.app.order.dto.OrderItemRequest;
import com.pizzeria.app.order.dto.OrderRequest;
import com.pizzeria.app.order.entity.Order;
import com.pizzeria.app.order.entity.OrderItem;
import com.pizzeria.app.order.entity.OrderStatus;
import com.pizzeria.app.order.exception.OrderNotFoundException;
import com.pizzeria.app.order.repository.OrderItemRepository;
import com.pizzeria.app.order.repository.OrderRepository;
import com.pizzeria.app.product.entity.Product;
import com.pizzeria.app.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final PricingService pricingService;
    private final OrderStatusTransitionValidator statusTransitionValidator;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         ProductService productService,
                         PricingService pricingService,
                         OrderStatusTransitionValidator statusTransitionValidator,
                         NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.pricingService = pricingService;
        this.statusTransitionValidator = statusTransitionValidator;
        this.notificationService = notificationService;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> search(String customerName) {
        return orderRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }

    @Transactional
    public Order create(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setCustomerAddress(request.customerAddress());
        order.setCouponCode(request.couponCode());

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = buildOrderItem(order, itemRequest);
            order.getItems().add(item);
            subtotal = subtotal.add(item.getLineTotal());
            totalQuantity += itemRequest.quantity();
        }

        PricingResult pricing = pricingService.calculate(subtotal, totalQuantity, request.couponCode());
        order.setSubtotal(pricing.subtotal());
        order.setDiscountAmount(pricing.discountAmount());
        order.setTotal(pricing.total());
        order.setStatus(OrderStatus.CREATED);

        Order saved = orderRepository.save(order);
        notificationService.sendOrderStatusNotification(saved);
        return saved;
    }

    @Transactional
    public Order updateStatus(Long id, String requestedStatus) {
        Order order = findById(id);
        OrderStatus newStatus = parseStatus(requestedStatus);
        statusTransitionValidator.validateTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
            notificationService.sendOrderStatusNotification(updated);
        }

        return updated;
    }

    @Transactional
    public void delete(Long id) {
        Order order = findById(id);
        orderItemRepository.deleteAll(order.getItems());
        orderRepository.deleteById(id);
    }

    private OrderItem buildOrderItem(Order order, OrderItemRequest itemRequest) {
        Product product = productService.findById(itemRequest.productId());
        if (!product.isAvailable()) {
            throw new BusinessValidationException("Product not available: " + product.getName());
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(itemRequest.quantity());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        return item;
    }

    private OrderStatus parseStatus(String value) {
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException("Unknown status: " + value);
        }
    }
}
