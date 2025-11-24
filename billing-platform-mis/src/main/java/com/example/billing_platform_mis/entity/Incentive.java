package com.example.billing_platform_mis.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "incentives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incentive {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "client_employee_id", nullable = false)
    private ClientEmployee clientEmployee;
    
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
    
    @Column(name = "distance_incentive", precision = 8, scale = 2)
    private BigDecimal distanceIncentive = BigDecimal.ZERO;
    
    @Column(name = "time_incentive", precision = 8, scale = 2)
    private BigDecimal timeIncentive = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}