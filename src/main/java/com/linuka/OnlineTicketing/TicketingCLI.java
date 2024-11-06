package com.linuka.OnlineTicketing;

import java.util.Scanner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketHandler;

public class TicketingCLI {

    private static final String WS_URI = "ws://localhost:8080/ws/ticketing";
    private WebSocketSession session;
    private int totalTickets;
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;

    public TicketingCLI() {
        try {
            connectToWebSocket();
        } catch (Exception e) {
            System.err.println("Error connecting to WebSocket: " + e.getMessage());
        }
    }

    private void configureSystem() {
        Scanner scanner = new Scanner(System.in);
        totalTickets = getValidInput(scanner, "Enter total tickets: ");
        ticketReleaseRate = getValidInput(scanner, "Enter ticket release rate: ");
        customerRetrievalRate = getValidInput(scanner, "Enter customer retrieval rate: ");
        maxTicketCapacity = getValidInput(scanner, "Enter max ticket capacity: ");
    }

    private int getValidInput(Scanner scanner, String prompt) {
        int input;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                if (input > 0) {
                    break;
                }
            } else {
                scanner.next(); // Clear invalid input
            }
            System.out.println("Invalid input. Please enter a positive integer.");
        }
        return input;
    }

    public void start() {
        // Start ticket handling operations
        System.out.println("Ticket handling operations started.");
    }

    public void stop() {
        // Stop ticket handling operations
        System.out.println("Ticket handling operations stopped.");
    }

    private void logTransaction(String message) {
        System.out.println(message);
        // Additional logging logic if needed
    }

    private void connectToWebSocket() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                System.out.println("Received from server: " + message.getPayload());
            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                System.out.println("WebSocket connection established.");
                TicketingCLI.this.session = session;
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                System.err.println("WebSocket error: " + exception.getMessage());
            }
        };
        client.doHandshake(handler, WS_URI);
    }

    public static void main(String[] args) {
        TicketingCLI cli = new TicketingCLI();
        cli.configureSystem();
        cli.start();
        // Main loop...
        cli.stop();
    }
}