package com.linuka.OnlineTicketing.controller;

import com.linuka.OnlineTicketing.entity.Ticket;
import com.linuka.OnlineTicketing.service.TicketService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public Ticket addTicket(@RequestBody Ticket ticket) {
        return ticketService.addTicket(ticket);
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PutMapping("/{id}/purchase")
    public Ticket purchaseTicket(@PathVariable Long id) {
        return ticketService.purchaseTicket(id);
    }
}