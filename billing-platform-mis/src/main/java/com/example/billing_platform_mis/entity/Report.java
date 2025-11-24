package com.example.billing_platform_mis.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;
    
    @Column(name = "report_type", nullable = false)
    private String reportType;
    
    @Column(name = "target_entity_id")
    private Long targetEntityId; // client_id, vendor_id, or employee_id
    
    @Column(name = "date_from")
    private LocalDate dateFrom;
    
    @Column(name = "date_to")
    private LocalDate dateTo;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}