package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Repository for Payout entity for financial data access
@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    
    //Find payout by trip ID (one-to-one relationship)
    Optional<Payout> findByTripId(Long tripId);
    
    //Find payouts by client-vendor relationship for vendor reporting
    List<Payout> findByClientVendorId(Long clientVendorId);
    
    //Find payouts by ClientVendor entity
    List<Payout> findByClientVendor(com.example.billing_platform_mis.entity.ClientVendor clientVendor);
    
    //Find payouts by ClientVendor and date range
    @Query("SELECT p FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.id = :clientVendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByClientVendorAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    //Find payouts by client-vendor relationship within date range
    @Query("SELECT p FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.id = :clientVendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByClientVendorIdAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
    
    //Find payouts by vendor ID for vendor self-service
    @Query("SELECT p FROM Payout p " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByVendorId(@Param("vendorId") Long vendorId);
    
    //Find payouts by vendor ID within date range
    @Query("SELECT p FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByVendorIdAndDateRange(@Param("vendorId") Long vendorId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    //Find payouts by client ID for client reporting
    @Query("SELECT p FROM Payout p " +
           "WHERE p.clientVendor.client.id = :clientId " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByClientId(@Param("clientId") Long clientId);
    
    //Find payouts by client ID within date range
    @Query("SELECT p FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByClientIdAndDateRange(@Param("clientId") Long clientId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    //Find payouts by vendor and client within date range
    @Query("SELECT p FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "AND p.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByVendorIdAndClientIdAndDateRange(@Param("vendorId") Long vendorId,
                                                     @Param("clientId") Long clientId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
    
    //Calculate total payout amount by vendor within date range
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPayoutByVendorAndDateRange(@Param("vendorId") Long vendorId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    //Calculate total payout amount by client within date range
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPayoutByClientAndDateRange(@Param("clientId") Long clientId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    //Calculate total payout amount by client-vendor relationship within date range
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.id = :clientVendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPayoutByClientVendorAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);
    
    //Calculate total base amount by vendor within date range
    @Query("SELECT COALESCE(SUM(p.baseAmount), 0) FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalBaseAmountByVendorAndDateRange(@Param("vendorId") Long vendorId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
    
    //Calculate total overage amount by vendor within date range
    @Query("SELECT COALESCE(SUM(p.distanceOverage + p.timeOverage), 0) FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalOverageByVendorAndDateRange(@Param("vendorId") Long vendorId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    //Count payouts by vendor within date range
    @Query("SELECT COUNT(p) FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    long countByVendorIdAndDateRange(@Param("vendorId") Long vendorId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    //Count payouts by client within date range
    @Query("SELECT COUNT(p) FROM Payout p " +
           "JOIN p.trip t " +
           "WHERE p.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    long countByClientIdAndDateRange(@Param("clientId") Long clientId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    //Find payouts with client and trip details for vendor reporting
    @Query("SELECT p FROM Payout p " +
           "JOIN FETCH p.clientVendor cv " +
           "JOIN FETCH cv.client " +
           "JOIN FETCH p.trip t " +
           "WHERE p.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByVendorIdWithDetailsAndDateRange(@Param("vendorId") Long vendorId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    //Find payouts with vendor and trip details for client reporting
    @Query("SELECT p FROM Payout p " +
           "JOIN FETCH p.clientVendor cv " +
           "JOIN FETCH cv.vendor " +
           "JOIN FETCH p.trip t " +
           "WHERE p.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    List<Payout> findByClientIdWithDetailsAndDateRange(@Param("clientId") Long clientId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    //Find recent payouts for dashboard (last 30 days)
    @Query("SELECT p FROM Payout p WHERE p.createdAt >= :thirtyDaysAgo ORDER BY p.createdAt DESC")
    List<Payout> findRecentPayouts(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    //Calculate average payout amount for analytics
    @Query("SELECT AVG(p.totalAmount) FROM Payout p")
    BigDecimal calculateAveragePayoutAmount();
    
    //Count total payouts for system analytics
    @Query("SELECT COUNT(p) FROM Payout p")
    long countTotalPayouts();
    
    //Calculate total payout amount for system analytics
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payout p")
    BigDecimal calculateTotalPayoutAmount();
    
    //Find top vendors by payout amount within date range
    @Query("SELECT cv.vendor, SUM(p.totalAmount) as totalPayout FROM Payout p " +
           "JOIN p.clientVendor cv " +
           "JOIN p.trip t " +
           "WHERE t.tripDate BETWEEN :startDate AND :endDate " +
           "GROUP BY cv.vendor " +
           "ORDER BY totalPayout DESC")
    List<Object[]> findTopVendorsByPayoutAmount(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    //Find vendor payout summary by client within date range
    @Query("SELECT cv.vendor, COUNT(p), SUM(p.totalAmount) FROM Payout p " +
           "JOIN p.clientVendor cv " +
           "JOIN p.trip t " +
           "WHERE cv.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "GROUP BY cv.vendor " +
           "ORDER BY SUM(p.totalAmount) DESC")
    List<Object[]> findVendorPayoutSummaryByClientAndDateRange(@Param("clientId") Long clientId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
    
    //Check if payout exists for a trip (to prevent duplicates)
    boolean existsByTripId(Long tripId);
}