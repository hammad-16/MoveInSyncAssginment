package com.example.billing_platform_mis.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_vendor_id", nullable = false)
    private ClientVendor clientVendor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;
    
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal distance;
    
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal duration; // in hours
    
    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;
    
    // Billing calculation fields
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    @Column(name = "employee_cost", precision = 10, scale = 2)
    private BigDecimal employeeCost;
    
    @Column(name = "vendor_cost", precision = 10, scale = 2)
    private BigDecimal vendorCost;
    
    @Column(name = "extra_distance_cost", precision = 10, scale = 2)
    private BigDecimal extraDistanceCost;
    
    @Column(name = "extra_time_cost", precision = 10, scale = 2)
    private BigDecimal extraTimeCost;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TripStatus status = TripStatus.COMPLETED;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}