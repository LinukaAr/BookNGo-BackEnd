package com.linuka.OnlineTicketing.producerconsumer;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import java.io.IOException;
import com.linuka.OnlineTicketing.entity.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

// Class to manage the ticket pool and handle ticket requests
public class TicketPool {
    private static final Logger logger = LogManager.getLogger(TicketPool.class);
    private final List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());
    private final PriorityQueue<TicketRequest> vipQueue = new PriorityQueue<>(Comparator.comparingInt(TicketRequest::getPriority).reversed()); // Max heap for VIP requests
    private int maxTicketCapacity;
    private WebSocketSession session;
    private TicketPoolListener listener;// Listener to notify when ticket count changes

    public TicketPool(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    // Initialize tickets with a WebSocket session
    public synchronized void initializeTickets(int totalTickets, WebSocketSession session) {
        this.session = session;
        initializeTickets(totalTickets);
    }

    // Initialize tickets
    public synchronized void initializeTickets(int totalTickets) {
        if (totalTickets > maxTicketCapacity) {
            throw new IllegalArgumentException("Total tickets exceed max capacity");// Check if total tickets exceed max capacity
        }
        for (int i = 0; i < totalTickets; i++) {
            tickets.add(new Ticket());// Add tickets to the pool
        }
        logger.info(totalTickets + " tickets added. Total tickets: " + tickets.size());
        System.out.println(totalTickets + " tickets added. Total tickets: " + tickets.size());

        String logMessage = "Added " + totalTickets + " tickets to the pool.";
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("{\"message\": \"" + logMessage + "\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        notifyListener();// Notify listener
    }

    // Add tickets to the pool
    public synchronized void addTickets(int count) {
        if (tickets.size() + count > maxTicketCapacity) {// Check if total tickets exceed max capacity
            logger.warn("Cannot add tickets. Exceeds max capacity.");
            System.out.println("Cannot add tickets. Exceeds max capacity.");
            return;
        }
        for (int i = 0; i < count; i++) {
            tickets.add(new Ticket());
        }
        logger.info(count + " tickets added. Total tickets: " + tickets.size());
        System.out.println(count + " tickets added. Total tickets: " + tickets.size());
        notifyListener();
    }

    // Remove tickets from the pool
    public synchronized void removeTicket(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Invalid ticket count");
        }

        // Process VIP queue first
        while (!vipQueue.isEmpty() && count > 0) {
            TicketRequest vipRequest = vipQueue.poll();// Remove the VIP request
            int ticketsToFulfill = Math.min(vipRequest.getCount(), count);// Get the minimum of the VIP request count and the remaining tickets
            fulfillRequest(vipRequest, ticketsToFulfill);// Fulfill the VIP request
            count -= ticketsToFulfill;
        }

        // Handle regular ticket purchases
        if (count > tickets.size()) {
            logger.warn("Not enough tickets available for regular customers.");
            throw new IllegalArgumentException("Not enough tickets available");
        }
        for (int i = 0; i < count; i++) {
            tickets.remove(0);
        }
        // logger.info(count + " Ticket(s) purchased by regular customer(s). Remaining tickets: " + tickets.size());
        System.out.println(count + " Ticket(s) purchased by regular customer(s). Remaining tickets: " + tickets.size());
        notifyListener();
    }

    // Request tickets with priority
    public synchronized void requestTickets(int count, int priority) {
        if (count <= 0) {
            throw new IllegalArgumentException("Invalid ticket count");
        }
        vipQueue.add(new TicketRequest(count, priority));// Add the VIP request to the queue
        logger.info("VIP Request added: " + count + " tickets with priority " + priority);
        System.out.println("VIP Request added: " + count + " tickets with priority " + priority);
    }

    // Fulfill VIP request
    private void fulfillRequest(TicketRequest request, int count) {
        for (int i = 0; i < count; i++) {
            tickets.remove(0);//    Remove the ticket from the pool
        }
        logger.info("VIP Request fulfilled: " + count + " tickets for priority " + request.getPriority());
        System.out.println("VIP Request fulfilled: " + count + " tickets for priority " + request.getPriority());
        notifyListener();
    }

    // Get the current ticket count
    public synchronized int getTicketCount() {
        return tickets.size();
    }

    // Set the listener to notify when ticket count changes
    public void setListener(TicketPoolListener listener) {
        this.listener = listener;
    }

    // Get the maximum ticket capacity
    public void setMaxTicketCapacity(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    //
    private void notifyListener() {
        if (listener != null) {
            listener.onTicketCountChanged(tickets.size());
        }
    }

    // Inner class to handle VIP ticket requests
    private static class TicketRequest {
        private final int count;
        private final int priority;

        public TicketRequest(int count, int priority) {
            this.count = count;
            this.priority = priority;
        }

        public int getCount() {
            return count;
        }

        public int getPriority() {
            return priority;
        }
    }

    // Interface to listen for ticket count changes
    public interface TicketPoolListener {
        void onTicketCountChanged(int newCount);
    }
}
