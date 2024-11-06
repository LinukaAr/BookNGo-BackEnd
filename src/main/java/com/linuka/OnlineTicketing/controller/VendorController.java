package com.linuka.OnlineTicketing.controller;

import com.linuka.OnlineTicketing.entity.Vendor;
import com.linuka.OnlineTicketing.service.VendorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping("/add")
    public Vendor addVendor(@RequestParam String name) {
        return vendorService.addVendor(name);
    }

    @DeleteMapping("/remove/{id}")
    public String removeVendor(@PathVariable Long id) {
        vendorService.removeVendor(id);
        return "Vendor removed";
    }
}
