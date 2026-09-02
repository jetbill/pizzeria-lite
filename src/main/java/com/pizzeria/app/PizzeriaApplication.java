package com.pizzeria.app;

import com.pizzeria.app.order.config.PricingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PricingProperties.class)
public class PizzeriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PizzeriaApplication.class, args);
    }
}
