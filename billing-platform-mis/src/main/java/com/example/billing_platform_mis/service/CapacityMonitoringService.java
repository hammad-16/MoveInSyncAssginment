package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.repository.VendorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Simple capacity monitoring and alert service
@Service
public class CapacityMonitoringService {
    
    @Autowired
    private VendorProfileRepository vendorProfileRepository;
    
    @Autowired
    private CapacityManagementService capacityManagementService;
    
    //Get system-wide capacity overview
    public Map<String, Object> getCapacityOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        List<VendorProfile> allVendors = vendorProfileRepository.findAll();
        List<VendorProfile> nearCapacity = vendorProfileRepository.findVendorsWithHighCapacityUtilization();
        List<VendorProfile> atCapacity = vendorProfileRepository.findVendorsAtFullCapacity();
        
        overview.put("totalVendors", allVendors.size());
        overview.put("vendorsNearCapacity", nearCapacity.size());
        overview.put("vendorsAtCapacity", atCapacity.size());
        overview.put("averageUtilization", calculateAverageUtilization());
        
        return overview;
    }
    
    //Check for capacity alerts
    public List<VendorProfile> getCapacityAlerts() {
        return vendorProfileRepository.findVendorsWithHighCapacityUtilization();
    }
    
    //Get vendor capacity status
    public Map<String, Object> getVendorCapacityStatus(Long vendorId) {
        Map<String, Object> status = new HashMap<>();
        
        VendorProfile vendor = vendorProfileRepository.findByUserId(vendorId).orElse(null);
        if (vendor == null) return status;
        
        double utilization = capacityManagementService.getCapacityUtilization(vendorId);
        boolean hasCapacity = capacityManagementService.hasAvailableCapacity(vendorId);
        
        status.put("vendorId", vendorId);
        status.put("maxCapacity", vendor.getMaxClientCapacity());
        status.put("utilizationPercent", utilization);
        status.put("hasAvailableCapacity", hasCapacity);
        status.put("alertLevel", getAlertLevel(utilization));
        
        return status;
    }
    
    private BigDecimal calculateAverageUtilization() {
        return vendorProfileRepository.calculateAverageCapacityUtilization();
    }
    
    private String getAlertLevel(double utilization) {
        if (utilization >= 100) return "CRITICAL";
        if (utilization >= 80) return "WARNING";
        return "NORMAL";
    }
}