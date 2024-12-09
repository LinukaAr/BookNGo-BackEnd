package com.linuka.OnlineTicketing.producerconsumer;

import com.linuka.OnlineTicketing.entity.Ticket;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.ArrayList;

public class TicketPool {
    private static final Logger logger = LogManager.getLogger(TicketPool.class);
    private final List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());
    private int maxTicketCapacity;
    private TicketPoolListener listener;

    public TicketPool(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    public synchronized void initializeTickets(int totalTickets) {
        if (totalTickets > maxTicketCapacity) {
            throw new IllegalArgumentException("Total tickets exceed max capacity");
        }
        for (int i = 0; i < totalTickets; i++) {
            tickets.add(new Ticket());
        }
        logger.info(totalTickets + " tickets added. Total tickets: " + tickets.size());
        System.out.println(totalTickets + " tickets added. Total tickets: " + tickets.size());
        notifyListener();
    }

    public synchronized void addTickets(int count) {
        if (tickets.size() + count > maxTicketCapacity) {
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

    public synchronized void removeTicket(int count) {
        if (count <= 0 || count > tickets.size()) {
            throw new IllegalArgumentException("Invalid ticket count");
        }
        for (int i = 0; i < count; i++) {
            tickets.remove(0);
        }
        logger.info(count + " Ticket(s) purchased. Remaining tickets: " + tickets.size());
        System.out.println(count + " Ticket(s) purchased. Remaining tickets: " + tickets.size());
    }

    public synchronized int getTicketCount() {
        return tickets.size();
    }

    public void setListener(TicketPoolListener listener) {
        this.listener = listener;
    }

    public void setMaxTicketCapacity(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onTicketCountChanged(tickets.size());
        }
    }

    public interface TicketPoolListener {
        void onTicketCountChanged(int newCount);
    }
}