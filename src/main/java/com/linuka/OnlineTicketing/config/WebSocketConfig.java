package com.linuka.OnlineTicketing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.linuka.OnlineTicketing.websocket.LogWebSocketHandler; 

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TicketWebSocketHandler ticketWebSocketHandler;
    private final LogWebSocketHandler logWebSocketHandler;

    // Constructor for WebSocketConfig class
    public WebSocketConfig(TicketWebSocketHandler ticketWebSocketHandler, LogWebSocketHandler logWebSocketHandler) {
        this.ticketWebSocketHandler = ticketWebSocketHandler;
        this.logWebSocketHandler = logWebSocketHandler;
    }

    @Override
    // Register WebSocket handlers
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ticketWebSocketHandler, "/ws/ticketing").setAllowedOrigins("*");// Register ticketWebSocketHandler
        registry.addHandler(logWebSocketHandler, "/logs").setAllowedOrigins("*");// Register logWebSocketHandler
    }
}