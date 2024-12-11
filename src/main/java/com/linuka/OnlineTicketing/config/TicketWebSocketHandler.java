package com.linuka.OnlineTicketing.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linuka.OnlineTicketing.producerconsumer.Customer;
import com.linuka.OnlineTicketing.producerconsumer.TicketPool;
import com.linuka.OnlineTicketing.producerconsumer.Vendor;
import com.linuka.OnlineTicketing.producerconsumer.VIPCustomer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Component
// WebSocket handler to handle ticket pool status updates
public class TicketWebSocketHandler extends TextWebSocketHandler implements TicketPool.TicketPoolListener {

    @Override
    // Handle ticket count changes
    public void onTicketCountChanged(int newCount) {
        // Implement the logic to handle the ticket count change
        System.out.println("Ticket count changed: " + newCount);
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage("{\"ticketCount\": " + newCount + "}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private final ObjectMapper objectMapper = new ObjectMapper();// Object mapper to convert JSON to Java objects
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();// List of WebSocket sessions
    private final TicketPool ticketPool;
    private final ReentrantLock lock = new ReentrantLock();// Lock to synchronize access to the ticket pool
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;
    private Thread[] vendorThreads;// Array of vendor threads
    private Thread[] customerThreads;// Array of customer threads
    private Thread[] vipCustomerThreads;// Array of VIP customer threads

    public TicketWebSocketHandler(TicketPool ticketPool) {
        this.ticketPool = ticketPool;
        this.ticketPool.setListener(this);
        }
        
        //
        private void sendTicketPoolStatus(WebSocketSession session) throws IOException {
            String statusMessage = "{\"status\": \"Ticket pool status message\"}";
            session.sendMessage(new TextMessage(statusMessage));
        }
    

    @Override
    // Handle new connections
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket connection established with session: " + session.getId());
        sendTicketPoolStatus(session);
    }

    @Override
    // Handle transport errors
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session);
        System.err.println("WebSocket transport error: " + exception.getMessage());
    }

    @Override
    // Handle connection closure
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket connection closed with session: " + session.getId() + ", status: " + status);
    }

    @Override
    // Handle incoming messages
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
            String action = jsonMessage.has("action") ? jsonMessage.get("action").asText() : null;

            if (action == null) {
                session.sendMessage(new TextMessage("Error: 'action' field is missing"));
                return;
            }

            if ("configure".equals(action)) {// Check if the action is to configure the system
                System.out.println("Configuration received: " + jsonMessage);
                try {
                    session.sendMessage(new TextMessage("{\"🛑Configuration Data Recieved\": " + jsonMessage + "}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ticketReleaseRate = jsonMessage.get("ticketReleaseRate").asInt();// Get the ticket release rate
                customerRetrievalRate = jsonMessage.get("customerRetrievalRate").asInt();// Get the customer retrieval rate
                maxTicketCapacity = jsonMessage.get("maxTicketCapacity").asInt();// Get the maximum ticket capacity
                ticketPool.setMaxTicketCapacity(maxTicketCapacity);
                ticketPool.addTickets(jsonMessage.get("totalTickets").asInt());
            } else if ("start".equals(action)) {
                System.out.println("Start action received with configuration: " + jsonMessage);
                initializeVendorsAndCustomers(session);// Initialize vendors and customers
            } else if ("stop".equals(action)) {
                System.out.println("Stop action received");
                stopVendorsAndCustomers(session);// Stop vendors and customers
            } else if ("add".equals(action)) {
                int tickets = jsonMessage.get("tickets").asInt();
                ticketPool.addTickets(tickets);//  Add tickets to the pool
                try {
                    session.sendMessage(new TextMessage("{\"message\": \"Tickets Added\", \"count\": " + tickets + "}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ("purchase".equals(action)) {
                int tickets = jsonMessage.get("tickets").asInt();
                for (int i = 0; i < tickets; i++) {
                    ticketPool.removeTicket(1);// Remove tickets from the pool
                }
                try {
                    session.sendMessage(new TextMessage("{\"message\": \"Tickets Purchased\", \"count\": " + tickets + "}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    session.sendMessage(new TextMessage("{\"error\": \"Unknown action '" + action + "'\"}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {// Handle exceptions
            System.err.println("Error handling message: " + e.getMessage());
            session.sendMessage(new TextMessage("Error: " + e.getMessage()));
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    // Initialize vendors and customers
    private void initializeVendorsAndCustomers(WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage("{\"🛑systemStatus\": \"start\"}"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create vendor threads
        Vendor[] vendors = new Vendor[10];
        vendorThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            vendors[i] = new Vendor(ticketPool, ticketReleaseRate, lock);// Create a new vendor
            vendorThreads[i] = new Thread(vendors[i], "Vendor-" + (i + 1));// Create a new thread
            vendorThreads[i].start();// Start the thread
        }

        // Create customer threads
        Customer[] customers = new Customer[5];
        customerThreads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            customers[i] = new Customer(ticketPool, customerRetrievalRate, lock, session);// Create a new customer
            customerThreads[i] = new Thread(customers[i], "Customer-" + (i + 1));
            customerThreads[i].start();
        }

        // Create VIP customer threads
        VIPCustomer[] vipCustomers = new VIPCustomer[2];
        vipCustomerThreads = new Thread[2];
        for (int i = 0; i < 2; i++) {
            vipCustomers[i] = new VIPCustomer(ticketPool, customerRetrievalRate*2, lock, session); // higher Proirity
            vipCustomerThreads[i] = new Thread(vipCustomers[i], "VIPCustomer-" + (i + 1));
            vipCustomerThreads[i].start();
        }
    }

    // Stop vendors and customers
    private void stopVendorsAndCustomers(WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage("{\"🛑systemStatus\": \"stop\"}"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (vendorThreads != null) {
            for (Thread thread : vendorThreads) {
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();// Interrupt the thread
                }
            }
        }

        if (customerThreads != null) {
            for (Thread thread : customerThreads) {// Check if the thread is alive
                if (thread != null && thread.isAlive()) {// Check if the thread is alive
                    thread.interrupt();
                }
            }
        }

        if (vipCustomerThreads != null) {
            for (Thread thread : vipCustomerThreads) {
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                }
            }
        }
    }
}