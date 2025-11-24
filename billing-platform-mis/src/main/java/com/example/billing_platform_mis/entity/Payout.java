package com.example.billing_platform_mis.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payout {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "client_vendor_id", nullable = false)
    private ClientVendor clientVendor;
    
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
    
    @Column(name = "base_amount", precision = 10, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;
    
    @Column(name = "distance_overage", precision = 8, scale = 2)
    private BigDecimal distanceOverage = BigDecimal.ZERO;
    
    @Column(name = "time_overage", precision = 8, scale = 2)
    private BigDecimal timeOverage = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}