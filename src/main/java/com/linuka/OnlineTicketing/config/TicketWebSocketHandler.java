package com.linuka.OnlineTicketing.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linuka.OnlineTicketing.producerconsumer.Customer;
import com.linuka.OnlineTicketing.producerconsumer.TicketPool;
import com.linuka.OnlineTicketing.producerconsumer.Vendor;
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
public class TicketWebSocketHandler extends TextWebSocketHandler implements TicketPool.TicketPoolListener {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private TicketPool ticketPool;
    private final ReentrantLock lock = new ReentrantLock();
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;
    private Thread[] vendorThreads;
    private Thread[] customerThreads;

    public TicketWebSocketHandler(TicketPool ticketPool) {
        this.ticketPool = ticketPool;
        this.ticketPool.setListener(this);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket connection established with session: " + session.getId());
        sendTicketPoolStatus(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session);
        System.err.println("WebSocket transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket connection closed with session: " + session.getId() + ", status: " + status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
        String action = jsonMessage.get("action").asText();

        System.out.println("Received message: " + jsonMessage);
        broadcast("Received message: " + jsonMessage);

        if ("configure".equals(action)) {
            int totalTickets = jsonMessage.get("totalTickets").asInt();
            maxTicketCapacity = jsonMessage.get("maxTicketCapacity").asInt();
            ticketReleaseRate = jsonMessage.get("ticketReleaseRate").asInt();
            customerRetrievalRate = jsonMessage.get("customerRetrievalRate").asInt();

            // *** CORRECTION:  Use the injected ticketPool, don't create a new one ***
            ticketPool.setMaxTicketCapacity(maxTicketCapacity); // Update the capacity
            ticketPool.initializeTickets(totalTickets);       // Initialize tickets


            // ticketPool.setMaxTicketCapacity(maxTicketCapacity);
            // ticketPool.addTickets(jsonMessage.get("ticketReleaseRate").asInt());
            // ticketPool.removeTicket(jsonMessage.get("customerRetrievalRate").asInt());
            // initializeVendorsAndCustomers();

        } else if ("start".equals(action)) {
            System.out.println("Start action received with configuration: " + jsonMessage);
            broadcast("Start action received with configuration: " + jsonMessage);
            initializeVendorsAndCustomers();
        } else if ("stop".equals(action)) {
            System.out.println("Stop action received");
            broadcast("Stop action received");
            stopVendorsAndCustomers();
        } else if ("add".equals(action)) {
            int tickets = jsonMessage.get("ticketReleaseRate").asInt();
            ticketPool.addTickets(tickets);
        } else if ("purchase".equals(action)) {
            int tickets = jsonMessage.get("tickets").asInt();
            ticketPool.removeTicket(tickets);
        }
    }

    private void initializeVendorsAndCustomers() {
        Vendor[] vendors = new Vendor[10];
        vendorThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            vendors[i] = new Vendor(ticketPool, ticketReleaseRate, lock);
            vendorThreads[i] = new Thread(vendors[i], "Vendor-" + (i + 1));
            vendorThreads[i].start();
        }

        Customer[] customers = new Customer[10];
        customerThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            customers[i] = new Customer(ticketPool, customerRetrievalRate, lock);
            customerThreads[i] = new Thread(customers[i], "Customer-" + (i + 1));
            customerThreads[i].start();
        }
    }

    private void stopVendorsAndCustomers() {
        for (Thread vendorThread : vendorThreads) {
            vendorThread.interrupt();
        }
        for (Thread customerThread : customerThreads) {
            customerThread.interrupt();
        }
    }

    @Override
    public void onTicketCountChanged(int newCount) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage("{\"ticketCount\": " + newCount + "}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendTicketPoolStatus(WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage("{\"ticketCount\": " + ticketPool.getTicketCount() + "}"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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