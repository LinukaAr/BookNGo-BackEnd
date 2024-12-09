package com.linuka.OnlineTicketing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String shortDescription;
    private String description;
    private String imageUrl;
    private LocalDateTime date;
    private String location;
    private double price;
    private String category;
    private int availableTickets;
    private String duration;
    private int capacity;
}