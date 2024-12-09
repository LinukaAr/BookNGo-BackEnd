package com.linuka.OnlineTicketing.config;

import com.linuka.OnlineTicketing.producerconsumer.TicketPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public TicketPool ticketPool() {
        return new TicketPool(100); 
    }
}