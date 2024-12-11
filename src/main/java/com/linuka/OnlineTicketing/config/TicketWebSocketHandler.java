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
    private final TicketPool ticketPool;
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
        try {
            JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
            String action = jsonMessage.has("action") ? jsonMessage.get("action").asText() : null;

            if (action == null) {
                session.sendMessage(new TextMessage("Error: 'action' field is missing"));
                return;
            }

            if ("configure".equals(action)) {
                System.out.println("Configuration received: " + jsonMessage);
                try {
                    session.sendMessage(new TextMessage("{\"🛑Configuration Data Recieved\": " + jsonMessage + "}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ticketReleaseRate = jsonMessage.get("ticketReleaseRate").asInt();
                customerRetrievalRate = jsonMessage.get("customerRetrievalRate").asInt();
                maxTicketCapacity = jsonMessage.get("maxTicketCapacity").asInt();
                ticketPool.setMaxTicketCapacity(maxTicketCapacity);
                ticketPool.addTickets(jsonMessage.get("totalTickets").asInt());
            } else if ("start".equals(action)) {
                System.out.println("Start action received with configuration: " + jsonMessage);
                initializeVendorsAndCustomers(session);
            } else if ("stop".equals(action)) {
                System.out.println("Stop action received");
                stopVendorsAndCustomers(session);
            } else if ("add".equals(action)) {
                int tickets = jsonMessage.get("tickets").asInt();
                ticketPool.addTickets(tickets);
                try {
                    session.sendMessage(new TextMessage("{\"message\": \"Tickets Added\", \"count\": " + tickets + "}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ("purchase".equals(action)) {
                int tickets = jsonMessage.get("tickets").asInt();
                for (int i = 0; i < tickets; i++) {
                    ticketPool.removeTicket(1);
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
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            session.sendMessage(new TextMessage("Error: " + e.getMessage()));
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private void initializeVendorsAndCustomers(WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage("{\"🛑systemStatus\": \"start\"}"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Vendor[] vendors = new Vendor[10];
        vendorThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            vendors[i] = new Vendor(ticketPool, ticketReleaseRate, lock);
            vendorThreads[i] = new Thread(vendors[i], "Vendor-" + (i + 1));
            vendorThreads[i].start();
        }

        Customer[] customers = new Customer[5];
        customerThreads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            customers[i] = new Customer(ticketPool, customerRetrievalRate, lock);
            customerThreads[i] = new Thread(customers[i], "Customer-" + (i + 1));
            customerThreads[i].start();
        }

    }

    private void stopVendorsAndCustomers(WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage("{\"🛑systemStatus\": \"stop\"}"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (vendorThreads != null) {
            for (Thread thread : vendorThreads) {
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                }
            }
        }

        if (customerThreads != null) {
            for (Thread thread : customerThreads) {
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                }
            }
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
}