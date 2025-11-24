package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.ClientEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Repository for ClientEmployee entity supporting client-employee relationship management
@Repository
public interface ClientEmployeeRepository extends JpaRepository<ClientEmployee, Long> {
    
    //Find client-employee relationship by client and employee IDs
    Optional<ClientEmployee> findByClientIdAndEmployeeId(Long clientId, Long employeeId);
    
    //Find all employees for a specific client
    List<ClientEmployee> findByClientId(Long clientId);
    
    //Find all client relationships for a specific employee
    List<ClientEmployee> findByEmployeeId(Long employeeId);
    
    //Find employees for a client with user details
    @Query("SELECT ce FROM ClientEmployee ce " +
           "JOIN FETCH ce.employee " +
           "WHERE ce.client.id = :clientId " +
           "ORDER BY ce.employee.name ASC")
    List<ClientEmployee> findByClientIdWithEmployeeDetails(@Param("clientId") Long clientId);
    
    //Find client-employee relationships with both user details
    @Query("SELECT ce FROM ClientEmployee ce " +
           "JOIN FETCH ce.client " +
           "JOIN FETCH ce.employee " +
           "WHERE ce.client.id = :clientId " +
           "ORDER BY ce.employee.name ASC")
    List<ClientEmployee> findByClientIdWithFullDetails(@Param("clientId") Long clientId);
    
    //Count employees for a client
    long countByClientId(Long clientId);
    
    //Check if client-employee relationship exists
    boolean existsByClientIdAndEmployeeId(Long clientId, Long employeeId);
    
    //Find employees by name pattern for a specific client
    @Query("SELECT ce FROM ClientEmployee ce " +
           "JOIN ce.employee e " +
           "WHERE ce.client.id = :clientId " +
           "AND LOWER(e.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "ORDER BY e.name ASC")
    List<ClientEmployee> findByClientIdAndEmployeeNameContaining(@Param("clientId") Long clientId,
                                                               @Param("namePattern") String namePattern);
    
    //Find all client-employee relationships for admin view
    @Query("SELECT ce FROM ClientEmployee ce " +
           "JOIN FETCH ce.client " +
           "JOIN FETCH ce.employee " +
           "ORDER BY ce.client.name ASC, ce.employee.name ASC")
    List<ClientEmployee> findAllWithDetails();
    
    //Count total client-employee relationships for analytics
    @Query("SELECT COUNT(ce) FROM ClientEmployee ce")
    long countTotalRelationships();
    
    //Find clients with employee count for analytics
    @Query("SELECT ce.client, COUNT(ce) as employeeCount FROM ClientEmployee ce " +
           "GROUP BY ce.client " +
           "ORDER BY employeeCount DESC")
    List<Object[]> findClientsWithEmployeeCount();
}