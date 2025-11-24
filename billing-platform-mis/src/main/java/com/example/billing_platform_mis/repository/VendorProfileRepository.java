package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.entity.BillingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

//Repository for VendorProfile entity supporting vendor matching and capacity management
@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    
    //Find vendor profile by user ID
    Optional<VendorProfile> findByUserId(Long userId);
    
    //Find vendor profiles by billing model for matching algorithm
    List<VendorProfile> findByBillingModel(BillingModel billingModel);
    
    //Find vendor profiles by billing model with available capacity
    @Query("SELECT vp FROM VendorProfile vp WHERE vp.billingModel = :billingModel " +
           "AND (SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id) < vp.maxClientCapacity")
    List<VendorProfile> findAvailableVendorsByBillingModel(@Param("billingModel") BillingModel billingModel);
    
    //Find vendor profiles with high capacity utilization (>80%)
    @Query("SELECT vp FROM VendorProfile vp WHERE " +
           "(SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id) * 1.0 / vp.maxClientCapacity > 0.8")
    List<VendorProfile> findVendorsWithHighCapacityUtilization();
    
    //Find vendor profiles at full capacity
    @Query("SELECT vp FROM VendorProfile vp WHERE " +
           "(SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id) >= vp.maxClientCapacity")
    List<VendorProfile> findVendorsAtFullCapacity();
    
    //Find vendor profiles with capacity details for matching algorithm
    @Query("SELECT vp, " +
           "(SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id) as currentLoad, " +
           "(vp.maxClientCapacity - (SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id)) as availableCapacity " +
           "FROM VendorProfile vp " +
           "WHERE vp.billingModel = :billingModel " +
           "ORDER BY availableCapacity DESC, vp.serviceQualityRating DESC")
    List<Object[]> findVendorsWithCapacityDetailsByBillingModel(@Param("billingModel") BillingModel billingModel);
    
    //Find vendors by service quality rating above threshold
    @Query("SELECT vp FROM VendorProfile vp WHERE vp.serviceQualityRating >= :minRating " +
           "ORDER BY vp.serviceQualityRating DESC")
    List<VendorProfile> findByServiceQualityRatingGreaterThanEqual(@Param("minRating") BigDecimal minRating);
    
    //Find vendors by billing model and minimum service quality
    @Query("SELECT vp FROM VendorProfile vp WHERE vp.billingModel = :billingModel " +
           "AND vp.serviceQualityRating >= :minRating " +
           "ORDER BY vp.serviceQualityRating DESC")
    List<VendorProfile> findByBillingModelAndMinServiceQuality(@Param("billingModel") BillingModel billingModel,
                                                             @Param("minRating") BigDecimal minRating);
    
    //Find vendors by geographic coverage pattern
    @Query("SELECT vp FROM VendorProfile vp WHERE LOWER(vp.geographicCoverage) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<VendorProfile> findByGeographicCoverageContaining(@Param("location") String location);
    
    //Find vendors by billing model and geographic coverage
    @Query("SELECT vp FROM VendorProfile vp WHERE vp.billingModel = :billingModel " +
           "AND LOWER(vp.geographicCoverage) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<VendorProfile> findByBillingModelAndGeographicCoverage(@Param("billingModel") BillingModel billingModel,
                                                              @Param("location") String location);
    
    //Find vendors with sufficient vehicle capacity
    @Query("SELECT vp FROM VendorProfile vp WHERE vp.availableVehicles >= :requiredVehicles")
    List<VendorProfile> findByAvailableVehiclesGreaterThanEqual(@Param("requiredVehicles") Integer requiredVehicles);
    
    //Find optimal vendors for matching (available capacity + good rating)
    @Query("SELECT vp FROM VendorProfile vp WHERE vp.billingModel = :billingModel " +
           "AND (SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id) < vp.maxClientCapacity " +
           "AND vp.serviceQualityRating >= :minRating " +
           "AND vp.availableVehicles >= :requiredVehicles " +
           "ORDER BY vp.serviceQualityRating DESC, " +
           "(vp.maxClientCapacity - (SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id)) DESC")
    List<VendorProfile> findOptimalVendorsForMatching(@Param("billingModel") BillingModel billingModel,
                                                    @Param("minRating") BigDecimal minRating,
                                                    @Param("requiredVehicles") Integer requiredVehicles);
    
    //Calculate average service quality rating by billing model
    @Query("SELECT AVG(vp.serviceQualityRating) FROM VendorProfile vp WHERE vp.billingModel = :billingModel")
    BigDecimal calculateAverageServiceQualityByBillingModel(@Param("billingModel") BillingModel billingModel);
    
    //Calculate average capacity utilization
    @Query("SELECT AVG((SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id) * 1.0 / vp.maxClientCapacity) " +
           "FROM VendorProfile vp WHERE vp.maxClientCapacity > 0")
    BigDecimal calculateAverageCapacityUtilization();
    
    //Count vendors by billing model
    long countByBillingModel(BillingModel billingModel);
    
    //Find vendor capacity summary for admin dashboard
    @Query("SELECT vp.user.name, vp.billingModel, vp.maxClientCapacity, " +
           "(SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = vp.user.id) as currentLoad, " +
           "vp.serviceQualityRating, vp.availableVehicles " +
           "FROM VendorProfile vp " +
           "ORDER BY vp.user.name ASC")
    List<Object[]> findVendorCapacitySummary();
    
    //Check if vendor profile exists for user
    boolean existsByUserId(Long userId);
}