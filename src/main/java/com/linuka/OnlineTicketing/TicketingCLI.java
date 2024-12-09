package com.linuka.OnlineTicketing;

import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.linuka.OnlineTicketing.producerconsumer.TicketPool;
import com.linuka.OnlineTicketing.producerconsumer.Vendor;
import com.linuka.OnlineTicketing.producerconsumer.Customer;

public class TicketingCLI {
    private static final String WS_URI = "ws://localhost:8080/ws/ticketing";
    private WebSocketSession session;
    private static final Logger logger = LogManager.getLogger(TicketingCLI.class);
    private int totalTickets;
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;
    private final ReentrantLock lock = new ReentrantLock();
    private TicketPool ticketPool;

    public TicketingCLI() {
        try {
            connectToWebSocket();
        } catch (Exception e) {
            logger.error("Error connecting to WebSocket: " + e.getMessage());
        }
    }

    private void configureSystem() {
        Scanner scanner = new Scanner(System.in);
        totalTickets = getValidInput(scanner, "Enter total tickets: ");
        ticketReleaseRate = getValidInput(scanner, "Enter ticket release rate: ");
        customerRetrievalRate = getValidInput(scanner, "Enter customer retrieval rate: ");
        maxTicketCapacity = getValidInput(scanner, "Enter max ticket capacity: ");
        ticketPool = new TicketPool(maxTicketCapacity);
        ticketPool.addTickets(totalTickets);
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
        logger.info("Ticket handling operations started.");
        sendMessage("{\"action\": \"start\"}");
        initializeVendorsAndCustomers();
    }

    public void stop() {
        logger.info("Ticket handling operations stopped.");
        sendMessage("{\"action\": \"stop\"}");
    }

    public void addTickets(int count) {
        lock.lock();
        try {
            ticketPool.addTickets(count);
            logTransaction(count + " tickets added.");
            sendMessage("{\"action\": \"add\", \"tickets\": " + count + "}");
        } finally {
            lock.unlock();
        }
    }

    private void logTransaction(String message) {
        logger.info(message);
    }

    private void connectToWebSocket() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                logger.info("Received from server: " + message.getPayload());
            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                logger.info("WebSocket connection established.");
                TicketingCLI.this.session = session;
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                logger.error("WebSocket error: " + exception.getMessage());
            }
        };
        client.doHandshake(handler, WS_URI).addCallback(
            result -> logger.info("WebSocket handshake successful"),
            ex -> logger.error("Error connecting to WebSocket: " + ex.getMessage())
        );
    }

    private void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                logger.error("Error sending message: " + e.getMessage());
            }
        } else {
            logger.error("WebSocket session is not open.");
        }
    }

    private void initializeVendorsAndCustomers() {
        Vendor[] vendors = new Vendor[10];
        Thread[] vendorThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            vendors[i] = new Vendor(ticketPool, ticketReleaseRate, lock);
            vendorThreads[i] = new Thread(vendors[i], "Vendor-" + (i + 1));
            vendorThreads[i].start();
        }

        Customer[] customers = new Customer[10];
        Thread[] customerThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            customers[i] = new Customer(ticketPool, customerRetrievalRate, lock);
            customerThreads[i] = new Thread(customers[i], "Customer-" + (i + 1));
            customerThreads[i].start();
        }
    }

    public static void main(String[] args) {
        TicketingCLI cli = new TicketingCLI();
        try {
            Thread.sleep(1000); // Wait for WebSocket connection to establish
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for WebSocket connection: " + e.getMessage());
        }
        cli.configureSystem();
        Scanner scanner = new Scanner(System.in);
        String command;
        while (true) {
            System.out.print("Enter command (start/stop/add/exit): ");
            command = scanner.nextLine();
            if ("start".equalsIgnoreCase(command)) {
                cli.start();
            } else if ("stop".equalsIgnoreCase(command)) {
                cli.stop();
            } else if ("add".equalsIgnoreCase(command)) {
                System.out.print("Enter number of tickets to add: ");
                int count = scanner.nextInt();
                cli.addTickets(count);
            } else if ("exit".equalsIgnoreCase(command)) {
                cli.stop();
                break;
            } else {
                System.out.println("Invalid command. Please enter 'start', 'stop', 'add', or 'exit'.");
            }
        }
        scanner.close();
    }
}