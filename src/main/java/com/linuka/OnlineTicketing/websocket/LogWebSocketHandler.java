package com.linuka.OnlineTicketing.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
// TextWebSocketHandler class to handle WebSocket messages
public class LogWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();// List to store WebSocket sessions

    @Override
    // Method to handle text messages
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {// Handle text messages
        System.out.println("Received message: " + message.getPayload());
    }

    @Override
    // Method to handle WebSocket connection closure
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket connection established with session: " + session.getId());
    }

    @Override
    // Method to handle WebSocket connection closure
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket connection closed with session: " + session.getId() + ", status: " + status);
    }

    // Method to broadcast messages to all WebSocket sessions
    public void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}