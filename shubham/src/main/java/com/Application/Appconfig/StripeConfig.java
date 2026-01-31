package com.Application.Appconfig;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe API initialized successfully");
    }
}