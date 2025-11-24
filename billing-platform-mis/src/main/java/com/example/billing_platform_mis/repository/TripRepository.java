package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//Repository for Trip entity with date range and tenant filtering capabilities
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    //Find trips by employee ID for employee self-service
    List<Trip> findByEmployeeId(Long employeeId);
    
    //Find trips by employee ID within date range
    @Query("SELECT t FROM Trip t WHERE t.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    //Find trips by client-vendor relationship
    List<Trip> findByClientVendorId(Long clientVendorId);
    
    //Find trips by client-vendor relationship within date range
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.id = :clientVendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByClientVendorIdAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    //Find trips by client ID (tenant filtering)
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.client.id = :clientId " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByClientId(@Param("clientId") Long clientId);
    
    //Find trips by client ID (alias for ClientController compatibility)
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.client.id = :clientId " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findTripsByClientId(@Param("clientId") Long clientId);
    
    //Find trips by employee and client
    @Query("SELECT t FROM Trip t WHERE t.employee.id = :employeeId " +
           "AND t.clientVendor.client.id = :clientId " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByEmployeeIdAndClientId(@Param("employeeId") Long employeeId, @Param("clientId") Long clientId);
    
    //Find trips by vendor and client
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.vendor.id = :vendorId " +
           "AND t.clientVendor.client.id = :clientId " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByVendorIdAndClientId(@Param("vendorId") Long vendorId, @Param("clientId") Long clientId);
    
    //Find trips by client ID within date range
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByClientIdAndDateRange(@Param("clientId") Long clientId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
    
    //Find trips by vendor ID (tenant filtering)
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.vendor.id = :vendorId " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByVendorId(@Param("vendorId") Long vendorId);
    
    //Find trips by vendor ID within date range
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByVendorIdAndDateRange(@Param("vendorId") Long vendorId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
    
    //Find trips by vendor and client within date range
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.vendor.id = :vendorId " +
           "AND t.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByVendorIdAndClientIdAndDateRange(@Param("vendorId") Long vendorId,
                                                   @Param("clientId") Long clientId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    //Find trips exceeding distance limits for incentive calculation
    @Query("SELECT t FROM Trip t WHERE t.distance > t.clientVendor.standardDistanceLimit " +
           "AND t.clientVendor.standardDistanceLimit > 0 " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findTripsExceedingDistanceLimit();
    
    //Find trips exceeding time limits for incentive calculation
    @Query("SELECT t FROM Trip t WHERE t.duration > t.clientVendor.standardTimeLimit " +
           "AND t.clientVendor.standardTimeLimit > 0 " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findTripsExceedingTimeLimit();
    
    //Find trips exceeding limits by client-vendor relationship
    @Query("SELECT t FROM Trip t WHERE t.clientVendor.id = :clientVendorId " +
           "AND (t.distance > t.clientVendor.standardDistanceLimit OR t.duration > t.clientVendor.standardTimeLimit) " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findTripsExceedingLimitsByClientVendorAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);
    
    //Calculate total distance for client-vendor relationship within date range
    @Query("SELECT COALESCE(SUM(t.distance), 0) FROM Trip t WHERE t.clientVendor.id = :clientVendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalDistanceByClientVendorAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
    
    //Calculate total duration for client-vendor relationship within date range
    @Query("SELECT COALESCE(SUM(t.duration), 0) FROM Trip t WHERE t.clientVendor.id = :clientVendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalDurationByClientVendorAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
    
    //Count trips by client-vendor relationship within date range
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.clientVendor.id = :clientVendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    long countByClientVendorIdAndDateRange(@Param("clientVendorId") Long clientVendorId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    //Count trips by employee within date range
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    long countByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
    
    //Find trips with employee details for client reporting
    @Query("SELECT t FROM Trip t " +
           "JOIN FETCH t.employee " +
           "WHERE t.clientVendor.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByClientIdWithEmployeeDetailsAndDateRange(@Param("clientId") Long clientId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
    
    //Find trips with client details for vendor reporting
    @Query("SELECT t FROM Trip t " +
           "JOIN FETCH t.clientVendor cv " +
           "JOIN FETCH cv.client " +
           "WHERE t.clientVendor.vendor.id = :vendorId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.tripDate DESC")
    List<Trip> findByVendorIdWithClientDetailsAndDateRange(@Param("vendorId") Long vendorId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
    
    //Find recent trips for dashboard (last 30 days)
    @Query("SELECT t FROM Trip t WHERE t.tripDate >= :thirtyDaysAgo ORDER BY t.tripDate DESC")
    List<Trip> findRecentTrips(@Param("thirtyDaysAgo") LocalDate thirtyDaysAgo);
    
    //Count total trips for system analytics
    @Query("SELECT COUNT(t) FROM Trip t")
    long countTotalTrips();
    
    //Count trips within date range for analytics
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.tripDate BETWEEN :startDate AND :endDate")
    long countTripsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}