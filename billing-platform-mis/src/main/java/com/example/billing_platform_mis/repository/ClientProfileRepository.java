package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.ClientProfile;
import com.example.billing_platform_mis.entity.BillingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfile, Long> {
    
    // Find client profile by user ID
    Optional<ClientProfile> findByUserId(Long userId);
    
    // Find clients by preferred billing model
    List<ClientProfile> findByPreferredBillingModel(BillingModel billingModel);
    
    // Find clients by budget range
    @Query("SELECT cp FROM ClientProfile cp WHERE cp.budgetRangeMin <= :maxBudget AND cp.budgetRangeMax >= :minBudget")
    List<ClientProfile> findByBudgetRange(@Param("minBudget") java.math.BigDecimal minBudget, 
                                         @Param("maxBudget") java.math.BigDecimal maxBudget);
    
    // Find clients by expected trip volume
    @Query("SELECT cp FROM ClientProfile cp WHERE cp.expectedMonthlyTrips BETWEEN :minTrips AND :maxTrips")
    List<ClientProfile> findByTripVolumeRange(@Param("minTrips") Integer minTrips, 
                                             @Param("maxTrips") Integer maxTrips);
}