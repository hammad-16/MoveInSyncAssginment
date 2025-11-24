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
@Table(name = "client_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_billing_model")
    private BillingModel preferredBillingModel;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "business_type")
    private String businessType;
    
    @Column(name = "expected_monthly_trips")
    private Integer expectedMonthlyTrips;
    
    @Column(name = "budget_range_min", precision = 10, scale = 2)
    private BigDecimal budgetRangeMin;
    
    @Column(name = "budget_range_max", precision = 10, scale = 2)
    private BigDecimal budgetRangeMax;
    
    @Column(name = "service_area", columnDefinition = "TEXT")
    private String serviceArea;
    
    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}