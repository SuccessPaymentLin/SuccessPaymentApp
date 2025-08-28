package com.example.paymentapi.config;

import com.wallee.sdk.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalleeConfig {

    @Value("${wallee.userId}")
    private Long userId;

    @Value("${wallee.apiKey}")
    private String apiKey;

    @Bean
    public ApiClient walleeApiClient() {
        return new ApiClient(userId, apiKey);
    }
}
