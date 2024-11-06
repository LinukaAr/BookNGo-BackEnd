package com.example.ticketingsystem.model;

import javax.persistence.*;

@Entity
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private int releaseRate;

    // Constructor, Getters, Setters
}
