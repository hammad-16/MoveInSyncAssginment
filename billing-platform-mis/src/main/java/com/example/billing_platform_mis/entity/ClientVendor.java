package com.example.billing_platform_mis.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_vendors", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "vendor_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientVendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_model", nullable = false)
    private BillingModel billingModel;
    
    // Base Rates
    @Column(name = "package_rate", precision = 10, scale = 2)
    private BigDecimal packageRate = BigDecimal.ZERO;
    
    @Column(name = "trip_rate", precision = 10, scale = 2)
    private BigDecimal tripRate = BigDecimal.ZERO;
    
    // Standard Limits (when overages trigger incentives)
    @Column(name = "standard_distance_limit", precision = 8, scale = 2)
    private BigDecimal standardDistanceLimit = BigDecimal.ZERO;
    
    @Column(name = "standard_time_limit", precision = 6, scale = 2)
    private BigDecimal standardTimeLimit = BigDecimal.ZERO;
    
    // Employee Incentive Rates (what employees get for overages)
    @Column(name = "employee_extra_distance_rate", precision = 8, scale = 2)
    private BigDecimal employeeExtraDistanceRate = BigDecimal.ZERO;
    
    @Column(name = "employee_extra_time_rate", precision = 8, scale = 2)
    private BigDecimal employeeExtraTimeRate = BigDecimal.ZERO;
    
    // Vendor Payout Rates (what vendors get for overages)
    @Column(name = "vendor_extra_distance_rate", precision = 8, scale = 2)
    private BigDecimal vendorExtraDistanceRate = BigDecimal.ZERO;
    
    @Column(name = "vendor_extra_time_rate", precision = 8, scale = 2)
    private BigDecimal vendorExtraTimeRate = BigDecimal.ZERO;
    
    // Capacity Planning
    @Column(name = "estimated_vehicles_needed")
    private Integer estimatedVehiclesNeeded = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}