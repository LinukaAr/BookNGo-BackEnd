package com.linuka.OnlineTicketing.config;

import com.linuka.OnlineTicketing.producerconsumer.TicketPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// Configuration class for the application
public class AppConfig {

    @Bean
    public TicketPool ticketPool() {
        return new TicketPool(100); 
    }
}