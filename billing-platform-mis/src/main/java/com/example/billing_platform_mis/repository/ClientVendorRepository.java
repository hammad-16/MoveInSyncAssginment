package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.ClientVendor;
import com.example.billing_platform_mis.entity.BillingModel;
import com.example.billing_platform_mis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Repository for ClientVendor entity with billing model filtering
@Repository
public interface ClientVendorRepository extends JpaRepository<ClientVendor, Long> {
    
    //Find client-vendor relationship by client and vendor IDs
    Optional<ClientVendor> findByClientIdAndVendorId(Long clientId, Long vendorId);
    
    //Find all vendor relationships for a specific client
    List<ClientVendor> findByClientId(Long clientId);
    
    //Find all client relationships for a specific vendor
    List<ClientVendor> findByVendorId(Long vendorId);
    
    //Find client-vendor relationships by billing model
    List<ClientVendor> findByBillingModel(BillingModel billingModel);
    
    //Find vendors for a client filtered by billing model
    @Query("SELECT cv FROM ClientVendor cv WHERE cv.client.id = :clientId AND cv.billingModel = :billingModel")
    List<ClientVendor> findByClientIdAndBillingModel(@Param("clientId") Long clientId, @Param("billingModel") BillingModel billingModel);
    
    //Find clients for a vendor filtered by billing model
    @Query("SELECT cv FROM ClientVendor cv WHERE cv.vendor.id = :vendorId AND cv.billingModel = :billingModel")
    List<ClientVendor> findByVendorIdAndBillingModel(@Param("vendorId") Long vendorId, @Param("billingModel") BillingModel billingModel);
    
    //Find all vendors with specific billing model for matching algorithm
    @Query("SELECT DISTINCT cv.vendor FROM ClientVendor cv WHERE cv.billingModel = :billingModel")
    List<User> findVendorsByBillingModel(@Param("billingModel") BillingModel billingModel);
    
    //Find available vendors (not at capacity) with specific billing model
    @Query("SELECT cv FROM ClientVendor cv JOIN VendorProfile vp ON cv.vendor.id = vp.user.id " +
           "WHERE cv.billingModel = :billingModel " +
           "AND (SELECT COUNT(cv2) FROM ClientVendor cv2 WHERE cv2.vendor.id = cv.vendor.id) < vp.maxClientCapacity")
    List<ClientVendor> findAvailableVendorsByBillingModel(@Param("billingModel") BillingModel billingModel);
    
    //Count clients per vendor for capacity management
    @Query("SELECT COUNT(cv) FROM ClientVendor cv WHERE cv.vendor.id = :vendorId")
    long countClientsByVendorId(@Param("vendorId") Long vendorId);
    
    //Find vendors with high capacity utilization (>80%)
    @Query("SELECT cv.vendor, COUNT(cv) as clientCount FROM ClientVendor cv " +
           "JOIN VendorProfile vp ON cv.vendor.id = vp.user.id " +
           "GROUP BY cv.vendor, vp.maxClientCapacity " +
           "HAVING (COUNT(cv) * 1.0 / vp.maxClientCapacity) > 0.8")
    List<Object[]> findVendorsWithHighCapacityUtilization();
    
    //Find client-vendor relationships with capacity details for admin dashboard
    @Query("SELECT cv, vp.maxClientCapacity, " +
           "(SELECT COUNT(cv2) FROM ClientVendor cv2 WHERE cv2.vendor.id = cv.vendor.id) as currentLoad " +
           "FROM ClientVendor cv " +
           "JOIN VendorProfile vp ON cv.vendor.id = vp.user.id " +
           "ORDER BY cv.createdAt DESC")
    List<Object[]> findAllWithCapacityInfo();
    
    //Find client-vendor relationships by client with vendor details
    @Query("SELECT cv FROM ClientVendor cv " +
           "JOIN FETCH cv.vendor " +
           "WHERE cv.client.id = :clientId " +
           "ORDER BY cv.vendor.name ASC")
    List<ClientVendor> findByClientIdWithVendorDetails(@Param("clientId") Long clientId);
    
    //Find client-vendor relationships by vendor with client details
    @Query("SELECT cv FROM ClientVendor cv " +
           "JOIN FETCH cv.client " +
           "WHERE cv.vendor.id = :vendorId " +
           "ORDER BY cv.client.name ASC")
    List<ClientVendor> findByVendorIdWithClientDetails(@Param("vendorId") Long vendorId);
    
    //Check if client-vendor relationship exists
    boolean existsByClientIdAndVendorId(Long clientId, Long vendorId);
    
    //Count total client-vendor relationships for system analytics
    @Query("SELECT COUNT(cv) FROM ClientVendor cv")
    long countTotalRelationships();
    
    //Count relationships by billing model for analytics
    long countByBillingModel(BillingModel billingModel);
    
    //Count vendors for a specific client
    long countByClientId(Long clientId);
}