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

    @PostMapping("/add")
    public String addTickets(@RequestParam int count, @RequestParam boolean isVIP) {
        ticketService.addTickets(count, isVIP);
        return count + " tickets added successfully";
    }

    @PostMapping("/purchase")
    public Ticket purchaseTicket(@RequestParam boolean isVIP) {
        return ticketService.purchaseTicket(isVIP);
    }
}