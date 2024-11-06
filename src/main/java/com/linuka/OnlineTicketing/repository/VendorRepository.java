package com.linuka.OnlineTicketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linuka.OnlineTicketing.entity.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
}