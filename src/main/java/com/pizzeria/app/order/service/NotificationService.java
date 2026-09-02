package com.pizzeria.app.order.service;

import com.pizzeria.app.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final String notificationProviderUrl;

    public NotificationService(@Value("${notification.provider-url}") String notificationProviderUrl) {
        this.notificationProviderUrl = notificationProviderUrl;
    }

    public void sendOrderStatusNotification(Order order) {
        log.info("Sending notification to {} via {} -> Order #{} is now {}",
                order.getCustomerPhone(), notificationProviderUrl, order.getId(), order.getStatus());
    }
}
