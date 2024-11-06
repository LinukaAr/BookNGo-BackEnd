package com.linuka.OnlineTicketing.service;

import com.linuka.OnlineTicketing.entity.Vendor;
import com.linuka.OnlineTicketing.repository.VendorRepository;
import org.springframework.stereotype.Service;

@Service
public class VendorService {
    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public Vendor addVendor(String name) {
        Vendor vendor = new Vendor();
        vendor.setName(name);
        vendor.setActive(true);
        return vendorRepository.save(vendor);
    }

    public void removeVendor(Long id) {
        vendorRepository.deleteById(id);
    }
}