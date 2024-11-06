package com.linuka.OnlineTicketing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
@Entity
@Getter
@Setter

public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isVIP;
    private LocalDateTime createdTime;
    private boolean isPurchased;

}
