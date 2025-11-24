package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.ClientVendor;
import com.example.billing_platform_mis.entity.Trip;
import com.example.billing_platform_mis.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TripBillingService {
    
    @Autowired
    private TripRepository tripRepository;
    
    //Calculate per-trip flat fees (used by trip and hybrid models)
    public BigDecimal calculateTripFees(Long clientVendorId, LocalDate startDate, LocalDate endDate, BigDecimal tripRate) {
        long tripCount = tripRepository.countByClientVendorIdAndDateRange(clientVendorId, startDate, endDate);
        return tripRate.multiply(BigDecimal.valueOf(tripCount));
    }
    
    //Get trip details for billing breakdown
    public List<Trip> getTripDetails(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        return tripRepository.findByClientVendorIdAndDateRange(clientVendorId, startDate, endDate);
    }
}