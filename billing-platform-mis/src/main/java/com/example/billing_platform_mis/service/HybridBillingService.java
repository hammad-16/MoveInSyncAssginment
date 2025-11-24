package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.ClientVendor;
import com.example.billing_platform_mis.repository.ClientVendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

//Service for hybrid billing model calculations
@Service
public class HybridBillingService {
    
    @Autowired
    private ClientVendorRepository clientVendorRepository;
    
    @Autowired
    private TripBillingService tripBillingService;
    
    //Calculate hybrid billing with detailed breakdown
    public Map<String, BigDecimal> calculateHybridBillingBreakdown(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        ClientVendor relationship = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        Map<String, BigDecimal> breakdown = new HashMap<>();
        
        //Base package component (fixed monthly fee)
        BigDecimal packageComponent = relationship.getPackageRate();
        breakdown.put("packageRate", packageComponent);
        
        //Trip component (variable based on usage)
        BigDecimal tripComponent = tripBillingService.calculateTripFees(
            clientVendorId, startDate, endDate, relationship.getTripRate());
        breakdown.put("tripCharges", tripComponent);
        
        //Total hybrid billing (package + trip charges only)
        BigDecimal total = packageComponent.add(tripComponent);
        breakdown.put("totalAmount", total);
        
        return breakdown;
    }
    
    //Simple hybrid calculation (package + trip charges only)
    public BigDecimal calculateSimpleHybridBilling(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        ClientVendor relationship = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        BigDecimal packageRate = relationship.getPackageRate();
        BigDecimal tripCharges = tripBillingService.calculateTripFees(
            clientVendorId, startDate, endDate, relationship.getTripRate());
        
        return packageRate.add(tripCharges);
    }
    
    //Calculate hybrid billing with usage thresholds
    public BigDecimal calculateThresholdBasedHybridBilling(Long clientVendorId, LocalDate startDate, LocalDate endDate, int freeTripsThreshold) {
        ClientVendor relationship = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        BigDecimal packageRate = relationship.getPackageRate();
        
        //Only charge for trips above the threshold
        long totalTrips = tripBillingService.getTripDetails(clientVendorId, startDate, endDate).size();
        long chargeableTrips = Math.max(0, totalTrips - freeTripsThreshold);
        
        BigDecimal additionalTripCharges = relationship.getTripRate()
            .multiply(BigDecimal.valueOf(chargeableTrips));
        
        return packageRate.add(additionalTripCharges);
    }
}