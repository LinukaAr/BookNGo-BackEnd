import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

// This class represents a pool of tickets that can be purchased by customers.
public class TicketPool {
    private final List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());// List of tickets available in the pool
    private final PriorityQueue<TicketRequest> vipQueue = new PriorityQueue<>(Comparator.comparingInt(TicketRequest::getPriority).reversed());// Priority queue for VIP requests
    private int maxTicketCapacity;
    private TicketPoolListener listener;

    public TicketPool(int maxTicketCapacity) {// Constructor for the TicketPool class
        this.maxTicketCapacity = maxTicketCapacity;
    }

    // Method to initialize the tickets in the pool
    public synchronized void initializeTickets(int totalTickets) {
        if (totalTickets > maxTicketCapacity) {
            throw new IllegalArgumentException("Total tickets exceed max capacity");// Throw an exception if the total tickets exceed the max capacity
        }
        for (int i = 0; i < totalTickets; i++) {
            tickets.add(new Ticket(i, "Ticket " + i));
        }
        System.out.println(totalTickets + " tickets added. Total tickets: " + tickets.size());
        logTransaction(totalTickets + " tickets added. Total tickets: " + tickets.size());
        notifyListener();
    }

    // Method to add tickets to the pool
    public synchronized void addTickets(int count) {
        if (tickets.size() + count > maxTicketCapacity) {
            System.out.println("Cannot add tickets. Exceeds max capacity.");
            logTransaction("Cannot add tickets. Exceeds max capacity.");
            return;
        }
        for (int i = 0; i < count; i++) {
            tickets.add(new Ticket(tickets.size(), "Ticket " + tickets.size()));
        }
        System.out.println(count + " tickets added. Total tickets: " + tickets.size());
        logTransaction(count + " tickets added. Total tickets: " + tickets.size());
        notifyListener();
    }

    // Method to remove tickets from the pool
    public synchronized void removeTicket(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Invalid ticket count");
        }

        while (!vipQueue.isEmpty() && count > 0) {
            TicketRequest vipRequest = vipQueue.poll();
            int ticketsToFulfill = Math.min(vipRequest.getCount(), count);
            fulfillRequest(vipRequest, ticketsToFulfill);
            count -= ticketsToFulfill;
        }

        if (count > tickets.size()) {
            throw new IllegalArgumentException("Not enough tickets available");
        }
        for (int i = 0; i < count; i++) {
            tickets.remove(0);
        }
        System.out.println(count + " Ticket(s) purchased by regular customer(s). Remaining tickets: " + tickets.size());
        logTransaction(count + " Ticket(s) purchased by regular customer(s). Remaining tickets: " + tickets.size());
        notifyListener();
    }

    // Method to request tickets with a given priority
    public synchronized void requestTickets(int count, int priority) {
        if (count <= 0) {
            throw new IllegalArgumentException("Invalid ticket count");
        }
        vipQueue.add(new TicketRequest(count, priority));
        System.out.println("VIP Request added: " + count + " tickets with priority " + priority);
        logTransaction("VIP Request added: " + count + " tickets with priority " + priority);
    }

    // Method to fulfill a VIP request
    private void fulfillRequest(TicketRequest request, int count) {
        for (int i = 0; i < count; i++) {
            tickets.remove(0);
        }
        System.out.println("VIP Request fulfilled: " + count + " tickets for priority " + request.getPriority());
        logTransaction("VIP Request fulfilled: " + count + " tickets for priority " + request.getPriority());
        notifyListener();
    }

    // Method to get the number of tickets in the pool
    public synchronized int getTicketCount() {
        return tickets.size();
    }

    // Method to set the listener for the ticket pool
    public void setListener(TicketPoolListener listener) {
        this.listener = listener;
    }

    // Method to get the maximum ticket capacity
    public void setMaxTicketCapacity(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    // Method to notify the listener when the ticket count changes
    private void notifyListener() {
        if (listener != null) {
            listener.onTicketCountChanged(tickets.size());
        }
    }

    // Method to log a transaction
    private void logTransaction(String message) {
        System.out.println("Logging transaction: " + message); // Debugging line
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("transactions.txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to represent a ticket request
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

    // Interface for the ticket pool listener
    public interface TicketPoolListener {
        void onTicketCountChanged(int newCount);
    }
}