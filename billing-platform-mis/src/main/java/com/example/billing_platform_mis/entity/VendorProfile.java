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
@Table(name = "vendor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_model", nullable = false)
    private BillingModel billingModel;
    
    @Column(name = "available_vehicles", nullable = false)
    private Integer availableVehicles = 0;
    
    @Column(name = "max_client_capacity", nullable = false)
    private Integer maxClientCapacity = 0;
    
    @Column(name = "service_quality_rating", precision = 3, scale = 2)
    private BigDecimal serviceQualityRating = BigDecimal.ZERO;
    
    @Column(name = "geographic_coverage", columnDefinition = "TEXT")
    private String geographicCoverage;
    
    // Rate management fields
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_billing_model")
    private BillingModel preferredBillingModel;
    
    @Column(name = "default_package_rate", precision = 10, scale = 2)
    private BigDecimal defaultPackageRate;
    
    @Column(name = "default_trip_rate", precision = 10, scale = 2)
    private BigDecimal defaultTripRate;
    
    @Column(name = "default_extra_distance_rate", precision = 10, scale = 2)
    private BigDecimal defaultExtraDistanceRate;
    
    @Column(name = "default_extra_time_rate", precision = 10, scale = 2)
    private BigDecimal defaultExtraTimeRate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}