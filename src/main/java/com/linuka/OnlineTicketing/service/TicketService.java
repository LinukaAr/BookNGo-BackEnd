package com.linuka.OnlineTicketing.service;

import com.linuka.OnlineTicketing.entity.Ticket;
import com.linuka.OnlineTicketing.repository.TicketRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final ReentrantLock lock = new ReentrantLock();

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public void addTickets(int count, boolean isVIP) {
        lock.lock();
        try {
            for (int i = 0; i < count; i++) {
                Ticket ticket = new Ticket();
                ticket.setVIP(isVIP);
                ticket.setCreatedTime(LocalDateTime.now());
                ticketRepository.save(ticket);
            }
        } finally {
            lock.unlock();
        }
    }

    public Ticket purchaseTicket(boolean isVIP) {
        lock.lock();
        try {
            List<Ticket> tickets = ticketRepository.findByIsVIPAndIsPurchased(isVIP, false);
            if (tickets.isEmpty()) throw new RuntimeException("No tickets available");
            Ticket ticket = tickets.get(0);
            ticket.setPurchased(true);
            return ticketRepository.save(ticket);
        } finally {
            lock.unlock();
        }
    }
}
