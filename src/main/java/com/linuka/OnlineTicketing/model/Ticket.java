package com.example.ticketingsystem.model;

import javax.persistence.*;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String status;

    // Constructor, Getters, Setters
}
