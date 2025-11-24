package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.BillingModel;
import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.repository.VendorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class VendorMatchingService {
    
    @Autowired
    private VendorProfileRepository vendorProfileRepository;
    
    //Find best vendor match for client requirements
    public VendorProfile findBestMatch(BillingModel billingModel, Integer requiredVehicles) {
        List<VendorProfile> availableVendors = vendorProfileRepository
            .findAvailableVendorsByBillingModel(billingModel);
        
        return availableVendors.stream()
            .filter(vendor -> vendor.getAvailableVehicles() >= requiredVehicles)
            .max((v1, v2) -> calculateScore(v1).compareTo(calculateScore(v2)))
            .orElse(null);
    }
    
    //Get all available vendors for billing model
    public List<VendorProfile> getAvailableVendors(BillingModel billingModel) {
        return vendorProfileRepository.findAvailableVendorsByBillingModel(billingModel);
    }
    
    //Simple scoring algorithm: service quality + capacity availability
    private BigDecimal calculateScore(VendorProfile vendor) {
        BigDecimal qualityScore = vendor.getServiceQualityRating();
        
       
        int currentLoad = getCurrentLoad(vendor);
        BigDecimal capacityRatio = BigDecimal.valueOf(currentLoad)
            .divide(BigDecimal.valueOf(vendor.getMaxClientCapacity()), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal capacityScore = BigDecimal.ONE.subtract(capacityRatio);
        
        return qualityScore.add(capacityScore);
    }
    
    private int getCurrentLoad(VendorProfile vendor) {
        long count = vendorProfileRepository.countByBillingModel(vendor.getBillingModel());
        return (int) count;
    }
}