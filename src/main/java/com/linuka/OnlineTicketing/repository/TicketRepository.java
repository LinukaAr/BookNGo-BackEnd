package com.linuka.OnlineTicketing.repository;

import com.linuka.OnlineTicketing.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}