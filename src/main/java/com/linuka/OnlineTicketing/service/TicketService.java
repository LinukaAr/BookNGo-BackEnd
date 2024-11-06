package com.linuka.OnlineTicketing.service;

import com.linuka.OnlineTicketing.entity.Ticket;
import com.linuka.OnlineTicketing.repository.TicketRepository;
import org.springframework.stereotype.Service;
import com.linuka.OnlineTicketing.exception.TicketNotFoundException;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket addTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket purchaseTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(
                () -> new TicketNotFoundException("Ticket not found")
        );
        ticket.setStatus("SOLD");
        return ticketRepository.save(ticket);
    }
}