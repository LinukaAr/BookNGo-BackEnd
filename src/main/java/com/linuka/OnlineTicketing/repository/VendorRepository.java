package com.example.ticketingsystem.repository;

import com.example.ticketingsystem.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
}
