package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.BillingModel;
import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.repository.VendorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class VendorFilterService {
    
    @Autowired
    private VendorProfileRepository vendorProfileRepository;
    
    //Filter vendors by billing model
    public List<VendorProfile> filterByBillingModel(BillingModel billingModel) {
        return vendorProfileRepository.findByBillingModel(billingModel);
    }
    
    //Filter vendors by geographic location
    public List<VendorProfile> filterByLocation(String location) {
        return vendorProfileRepository.findByGeographicCoverageContaining(location);
    }
    
    //Filter vendors by billing model and location
    public List<VendorProfile> filterByBillingModelAndLocation(BillingModel billingModel, String location) {
        return vendorProfileRepository.findByBillingModelAndGeographicCoverage(billingModel, location);
    }
    
    //Filter vendors by minimum service quality
    public List<VendorProfile> filterByMinServiceQuality(BigDecimal minRating) {
        return vendorProfileRepository.findByServiceQualityRatingGreaterThanEqual(minRating);
    }
    
    //Filter vendors by minimum vehicle count
    public List<VendorProfile> filterByMinVehicles(Integer minVehicles) {
        return vendorProfileRepository.findByAvailableVehiclesGreaterThanEqual(minVehicles);
    }
}