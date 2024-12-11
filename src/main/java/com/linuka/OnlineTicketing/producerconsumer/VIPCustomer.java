package com.linuka.OnlineTicketing.producerconsumer;

import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import org.springframework.web.socket.TextMessage;

// Inheriting the properties and methods of the Customer class
public class VIPCustomer extends Customer {
    private int priorityLevel;

    // Constructor for VIPCustomer class
    public VIPCustomer(TicketPool ticketPool, int retrievalRate, ReentrantLock lock, WebSocketSession session) {
        super(ticketPool, retrievalRate, lock, session);
        this.priorityLevel = 1; // Default priority level
    }

    public VIPCustomer(TicketPool ticketPool, int retrievalRate, ReentrantLock lock, int priorityLevel, WebSocketSession session) {
        super(ticketPool, retrievalRate, lock, session);// Call the constructor of the superclass
        setPriorityLevel(priorityLevel);// Set the priority level
    }

    // Get the priority level
    public int getPriorityLevel() {
        return priorityLevel;
    }

    // Set the priority level
    public void setPriorityLevel(int priorityLevel) {
        if (priorityLevel <= 0) {
            throw new IllegalArgumentException("Priority level must be greater than zero.");
        }
        this.priorityLevel = priorityLevel;
    }

    @Override
    // Run method for the VIPCustomer thread
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {// Check if the thread is interrupted
            lock.lock();
            try {
                ticketPool.removeTicket(customerRetrievalRate);// Remove tickets from the ticket pool
                String logMessage = Thread.currentThread().getName() + " (VIP) purchased " + customerRetrievalRate + " tickets.";
                System.out.println(logMessage);
                try {
                    session.sendMessage(new TextMessage("{\"message\": \"" + logMessage + "\"}"));// Send a message to the session
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                lock.unlock();
            }
            try {
                Thread.sleep(1000); // Simulate time taken to purchase tickets
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}