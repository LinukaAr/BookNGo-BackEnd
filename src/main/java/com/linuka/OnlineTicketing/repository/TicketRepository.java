package com.linuka.OnlineTicketing.repository;

import com.linuka.OnlineTicketing.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByIsVIPAndIsPurchased(boolean isVIP, boolean isPurchased);
}