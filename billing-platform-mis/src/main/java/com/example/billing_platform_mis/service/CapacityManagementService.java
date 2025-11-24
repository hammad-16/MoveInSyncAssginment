package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.repository.ClientVendorRepository;
import com.example.billing_platform_mis.repository.VendorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//Simple capacity management and validation service
@Service
public class CapacityManagementService {
    
    @Autowired
    private VendorProfileRepository vendorProfileRepository;
    
    @Autowired
    private ClientVendorRepository clientVendorRepository;
    
    //Check if vendor has available capacity
    public boolean hasAvailableCapacity(Long vendorId) {
        VendorProfile vendor = vendorProfileRepository.findByUserId(vendorId).orElse(null);
        if (vendor == null) return false;
        
        long currentLoad = clientVendorRepository.countClientsByVendorId(vendorId);
        return currentLoad < vendor.getMaxClientCapacity();
    }
    
    //Get current capacity utilization percentage
    public double getCapacityUtilization(Long vendorId) {
        VendorProfile vendor = vendorProfileRepository.findByUserId(vendorId).orElse(null);
        if (vendor == null || vendor.getMaxClientCapacity() == 0) return 0.0;
        
        long currentLoad = clientVendorRepository.countClientsByVendorId(vendorId);
        return (double) currentLoad / vendor.getMaxClientCapacity() * 100;
    }
    
    //Get vendors approaching capacity limit (>80%)
    public List<VendorProfile> getVendorsNearCapacity() {
        return vendorProfileRepository.findVendorsWithHighCapacityUtilization();
    }
    
    //Get vendors at full capacity
    public List<VendorProfile> getVendorsAtFullCapacity() {
        return vendorProfileRepository.findVendorsAtFullCapacity();
    }
    
    //Validate if vendor can take new client
    public boolean canAcceptNewClient(Long vendorId) {
        return hasAvailableCapacity(vendorId);
    }
}