package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.Incentive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Repository for Incentive entity for financial data access
@Repository
public interface IncentiveRepository extends JpaRepository<Incentive, Long> {
    
    //Find incentive by trip ID (one-to-one relationship)
    Optional<Incentive> findByTripId(Long tripId);
    
    //Find incentives by employee ID for employee self-service
    @Query("SELECT i FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "WHERE ce.employee.id = :employeeId " +
           "ORDER BY i.createdAt DESC")
    List<Incentive> findByEmployeeId(@Param("employeeId") Long employeeId);
    
    //Find incentives by employee ID within date range
    @Query("SELECT i FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY i.createdAt DESC")
    List<Incentive> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    //Find incentives by client ID for client reporting
    @Query("SELECT i FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "WHERE ce.client.id = :clientId " +
           "ORDER BY i.createdAt DESC")
    List<Incentive> findByClientId(@Param("clientId") Long clientId);
    
    //Find incentives by client ID within date range
    @Query("SELECT i FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY i.createdAt DESC")
    List<Incentive> findByClientIdAndDateRange(@Param("clientId") Long clientId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    
    //Find incentives by client-employee relationship
    List<Incentive> findByClientEmployeeId(Long clientEmployeeId);
    
    //Find incentives by client-employee relationship within date range
    @Query("SELECT i FROM Incentive i " +
           "JOIN i.trip t " +
           "WHERE i.clientEmployee.id = :clientEmployeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY i.createdAt DESC")
    List<Incentive> findByClientEmployeeIdAndDateRange(@Param("clientEmployeeId") Long clientEmployeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
    
    //Calculate total incentive amount by employee within date range
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalIncentiveByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
    
    //Calculate total incentive amount by client within date range
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalIncentiveByClientAndDateRange(@Param("clientId") Long clientId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
    
    //Calculate total distance incentives by employee within date range
    @Query("SELECT COALESCE(SUM(i.distanceIncentive), 0) FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalDistanceIncentiveByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                                   @Param("startDate") LocalDate startDate,
                                                                   @Param("endDate") LocalDate endDate);
    
    //Calculate total time incentives by employee within date range
    @Query("SELECT COALESCE(SUM(i.timeIncentive), 0) FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalTimeIncentiveByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                               @Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);
    
    //Count incentives by employee within date range
    @Query("SELECT COUNT(i) FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    long countByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
    
    //Count incentives by client within date range
    @Query("SELECT COUNT(i) FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE ce.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate")
    long countByClientIdAndDateRange(@Param("clientId") Long clientId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    //Find incentives with employee and trip details for client reporting
    @Query("SELECT i FROM Incentive i " +
           "JOIN FETCH i.clientEmployee ce " +
           "JOIN FETCH ce.employee " +
           "JOIN FETCH i.trip t " +
           "WHERE ce.client.id = :clientId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY i.createdAt DESC")
    List<Incentive> findByClientIdWithDetailsAndDateRange(@Param("clientId") Long clientId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
    
    //Find incentives with trip details for employee self-service
    @Query("SELECT i FROM Incentive i " +
           "JOIN FETCH i.trip t " +
           "JOIN i.clientEmployee ce " +
           "WHERE ce.employee.id = :employeeId " +
           "AND t.tripDate BETWEEN :startDate AND :endDate " +
           "ORDER BY i.createdAt DESC")
    List<Incentive> findByEmployeeIdWithTripDetailsAndDateRange(@Param("employeeId") Long employeeId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
    
    //Find recent incentives for dashboard (last 30 days)
    @Query("SELECT i FROM Incentive i WHERE i.createdAt >= :thirtyDaysAgo ORDER BY i.createdAt DESC")
    List<Incentive> findRecentIncentives(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    //Calculate average incentive amount for analytics
    @Query("SELECT AVG(i.totalAmount) FROM Incentive i")
    BigDecimal calculateAverageIncentiveAmount();
    
    //Count total incentives for system analytics
    @Query("SELECT COUNT(i) FROM Incentive i")
    long countTotalIncentives();
    
    //Calculate total incentive amount for system analytics
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Incentive i")
    BigDecimal calculateTotalIncentiveAmount();
    
    //Find top employees by incentive amount within date range
    @Query("SELECT ce.employee, SUM(i.totalAmount) as totalIncentive FROM Incentive i " +
           "JOIN i.clientEmployee ce " +
           "JOIN i.trip t " +
           "WHERE t.tripDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ce.employee " +
           "ORDER BY totalIncentive DESC")
    List<Object[]> findTopEmployeesByIncentiveAmount(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    //Check if incentive exists for a trip (to prevent duplicates)
    boolean existsByTripId(Long tripId);
}