package com.linuka.OnlineTicketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.linuka.OnlineTicketing.entity") 
public class OnlineTicketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineTicketingApplication.class, args);
    }
}