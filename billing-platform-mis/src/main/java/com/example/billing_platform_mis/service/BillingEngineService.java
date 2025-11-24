package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.*;
import com.example.billing_platform_mis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//Core billing engine for all billing model calculations
@Service
public class BillingEngineService {
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private ClientVendorRepository clientVendorRepository;
    
    @Autowired
    private PayoutRepository payoutRepository;
    
    @Autowired
    private IncentiveRepository incentiveRepository;
    
    //Calculate package model billing for a client-vendor relationship
    public BigDecimal calculatePackageBilling(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        ClientVendor relationship = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        if (relationship.getBillingModel() != BillingModel.PACKAGE) {
            throw new IllegalArgumentException("Not a package billing model");
        }
        
        //Package model: fixed monthly rate regardless of trip count
        return relationship.getPackageRate();
    }
    
    //Calculate trip model billing
    public BigDecimal calculateTripBilling(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        ClientVendor relationship = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        if (relationship.getBillingModel() != BillingModel.TRIP) {
            throw new IllegalArgumentException("Not a trip billing model");
        }
        
        //Trip model: rate per trip
        long tripCount = tripRepository.countByClientVendorIdAndDateRange(clientVendorId, startDate, endDate);
        return relationship.getTripRate().multiply(BigDecimal.valueOf(tripCount));
    }
    
    //Calculate hybrid model billing
    public BigDecimal calculateHybridBilling(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        ClientVendor relationship = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        if (relationship.getBillingModel() != BillingModel.HYBRID) {
            throw new IllegalArgumentException("Not a hybrid billing model");
        }
        
        //Hybrid model: base package rate + per-trip charges
        BigDecimal baseAmount = relationship.getPackageRate();
        long tripCount = tripRepository.countByClientVendorIdAndDateRange(clientVendorId, startDate, endDate);
        BigDecimal tripCharges = relationship.getTripRate().multiply(BigDecimal.valueOf(tripCount));
        
        return baseAmount.add(tripCharges);
    }
    
    //Main billing calculation method - routes to appropriate model
    public BigDecimal calculateBilling(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        ClientVendor relationship = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        switch (relationship.getBillingModel()) {
            case PACKAGE:
                return calculatePackageBilling(clientVendorId, startDate, endDate);
            case TRIP:
                return calculateTripBilling(clientVendorId, startDate, endDate);
            case HYBRID:
                return calculateHybridBilling(clientVendorId, startDate, endDate);
            default:
                throw new IllegalArgumentException("Unknown billing model");
        }
    }
}