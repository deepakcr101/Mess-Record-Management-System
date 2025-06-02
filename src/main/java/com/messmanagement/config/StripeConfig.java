package com.messmanagement.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = secretKey;
        // You can also set API version, connect timeout, etc. if needed
        // Stripe.setApiVersion("2022-11-15"); // Example: set a specific API version
    }
}