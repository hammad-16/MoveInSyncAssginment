package com.example.billing_platform_mis.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.billing_platform_mis.entity.BillingModel;
import com.example.billing_platform_mis.entity.ClientProfile;
import com.example.billing_platform_mis.entity.ClientVendor;
import com.example.billing_platform_mis.entity.User;
import com.example.billing_platform_mis.entity.UserRole;
import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.repository.ClientProfileRepository;
import com.example.billing_platform_mis.repository.ClientVendorRepository;
import com.example.billing_platform_mis.repository.UserRepository;
import com.example.billing_platform_mis.repository.VendorProfileRepository;
import com.example.billing_platform_mis.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VendorProfileRepository vendorProfileRepository;
    
    @Autowired
    private ClientVendorRepository clientVendorRepository;
    
    @Autowired
    private ClientProfileRepository clientProfileRepository;
    

    
    // Get all vendors with their profile information
    @GetMapping("/vendors")
    public ResponseEntity<?> getAllVendors() {
        try {
            List<User> vendors = userRepository.findByRole(UserRole.VENDOR);
            
            List<Map<String, Object>> vendorDetails = vendors.stream()
                .map(vendor -> {
                    Map<String, Object> vendorInfo = new HashMap<>();
                    vendorInfo.put("id", vendor.getId());
                    vendorInfo.put("name", vendor.getName());
                    vendorInfo.put("email", vendor.getEmail());
                    vendorInfo.put("role", vendor.getRole().toString());
                    vendorInfo.put("createdAt", vendor.getCreatedAt());
                    
                    // Get vendor profile information
                    Optional<VendorProfile> profile = vendorProfileRepository.findByUserId(vendor.getId());
                    if (profile.isPresent()) {
                        VendorProfile vp = profile.get();
                        // Use preferredBillingModel if set, otherwise fall back to billingModel
                        BillingModel billingModel = vp.getPreferredBillingModel() != null ? 
                            vp.getPreferredBillingModel() : vp.getBillingModel();
                        vendorInfo.put("preferredBillingModel", billingModel != null ? 
                            billingModel.toString() : "NOT_SPECIFIED");
                        vendorInfo.put("maxClientCapacity", vp.getMaxClientCapacity());
                        vendorInfo.put("availableVehicles", vp.getAvailableVehicles());
                        vendorInfo.put("serviceQualityRating", vp.getServiceQualityRating());
                        vendorInfo.put("geographicCoverage", vp.getGeographicCoverage());
                        vendorInfo.put("defaultPackageRate", vp.getDefaultPackageRate());
                        vendorInfo.put("defaultTripRate", vp.getDefaultTripRate());
                        
                        // Calculate current load
                        long currentLoadLong = clientVendorRepository.countClientsByVendorId(vendor.getId());
                        int currentLoad = (int) currentLoadLong;
                        vendorInfo.put("currentLoad", currentLoad);
                        vendorInfo.put("availableCapacity", vp.getMaxClientCapacity() - currentLoad);
                    } else {
                        // Default values for vendors without profiles
                        vendorInfo.put("preferredBillingModel", "NOT_SPECIFIED");
                        vendorInfo.put("maxClientCapacity", 0);
                        vendorInfo.put("availableVehicles", 0);
                        vendorInfo.put("serviceQualityRating", 0.0);
                        vendorInfo.put("geographicCoverage", "Not specified");
                        vendorInfo.put("defaultPackageRate", null);
                        vendorInfo.put("defaultTripRate", null);
                        vendorInfo.put("currentLoad", 0);
                        vendorInfo.put("availableCapacity", 0);
                    }
                    
                    return vendorInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(vendorDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get all employees
    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<User> employees = userRepository.findByRole(UserRole.EMPLOYEE);
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get all clients with their profile information
    @GetMapping("/clients")
    public ResponseEntity<?> getAllClients() {
        try {
            List<User> clients = userRepository.findByRole(UserRole.CLIENT);
            
            List<Map<String, Object>> clientDetails = clients.stream()
                .map(client -> {
                    Map<String, Object> clientInfo = new HashMap<>();
                    clientInfo.put("id", client.getId());
                    clientInfo.put("name", client.getName());
                    clientInfo.put("email", client.getEmail());
                    clientInfo.put("role", client.getRole().toString());
                    clientInfo.put("createdAt", client.getCreatedAt());
                    
                    // Get client profile information
                    Optional<ClientProfile> profile = clientProfileRepository.findByUserId(client.getId());
                    if (profile.isPresent()) {
                        ClientProfile cp = profile.get();
                        clientInfo.put("preferredBillingModel", cp.getPreferredBillingModel() != null ? 
                            cp.getPreferredBillingModel().toString() : "NOT_SPECIFIED");
                        clientInfo.put("companyName", cp.getCompanyName());
                        clientInfo.put("businessType", cp.getBusinessType());
                        clientInfo.put("expectedMonthlyTrips", cp.getExpectedMonthlyTrips());
                        clientInfo.put("budgetRangeMin", cp.getBudgetRangeMin());
                        clientInfo.put("budgetRangeMax", cp.getBudgetRangeMax());
                        clientInfo.put("serviceArea", cp.getServiceArea());
                        clientInfo.put("specialRequirements", cp.getSpecialRequirements());
                        
                        // Get assigned vendor information
                        List<ClientVendor> assignments = clientVendorRepository.findByClientId(client.getId());
                        clientInfo.put("assignedVendors", assignments.size());
                        
                        // Include the first assigned vendor details (for display purposes)
                        if (!assignments.isEmpty()) {
                            ClientVendor firstAssignment = assignments.get(0);
                            Map<String, Object> assignedVendor = new HashMap<>();
                            assignedVendor.put("id", firstAssignment.getVendor().getId());
                            assignedVendor.put("name", firstAssignment.getVendor().getName());
                            assignedVendor.put("email", firstAssignment.getVendor().getEmail());
                            assignedVendor.put("billingModel", firstAssignment.getBillingModel().toString());
                            clientInfo.put("assignedVendor", assignedVendor);
                        } else {
                            clientInfo.put("assignedVendor", null);
                        }
                    } else {
                        // Default values for clients without profiles
                        clientInfo.put("preferredBillingModel", "NOT_SPECIFIED");
                        clientInfo.put("companyName", client.getName());
                        clientInfo.put("businessType", "Not specified");
                        clientInfo.put("expectedMonthlyTrips", 0);
                        clientInfo.put("budgetRangeMin", null);
                        clientInfo.put("budgetRangeMax", null);
                        clientInfo.put("serviceArea", "Not specified");
                        clientInfo.put("specialRequirements", null);
                        clientInfo.put("assignedVendors", 0);
                    }
                    
                    return clientInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(clientDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Create vendor profile
    @PostMapping("/vendors/{vendorId}/profile")
    public ResponseEntity<?> createVendorProfile(@PathVariable @NotNull @Positive Long vendorId, 
                                               @Valid @RequestBody VendorProfileRequest request) {
        try {
            User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
            
            if (vendor.getRole() != UserRole.VENDOR) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is not a vendor"));
            }
            
            VendorProfile profile = new VendorProfile();
            profile.setUser(vendor);
            profile.setBillingModel(BillingModel.valueOf(request.getBillingModel().toUpperCase()));
            profile.setAvailableVehicles(request.getAvailableVehicles());
            profile.setMaxClientCapacity(request.getMaxClientCapacity());
            profile.setServiceQualityRating(request.getServiceQualityRating());
            profile.setGeographicCoverage(request.getGeographicCoverage());
            
            VendorProfile savedProfile = vendorProfileRepository.save(profile);
            return ResponseEntity.ok(savedProfile);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Assign vendor to client
    @PostMapping("/assign-vendor")
    public ResponseEntity<?> assignVendorToClient(@Valid @RequestBody VendorAssignmentRequest request) {
        try {
            User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
            User vendor = userRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
            
            if (client.getRole() != UserRole.CLIENT) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid client"));
            }
            if (vendor.getRole() != UserRole.VENDOR) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid vendor"));
            }
            
            ClientVendor assignment = new ClientVendor();
            assignment.setClient(client);
            assignment.setVendor(vendor);
            assignment.setBillingModel(BillingModel.valueOf(request.getBillingModel().toUpperCase()));
            assignment.setPackageRate(request.getPackageRate());
            assignment.setTripRate(request.getTripRate());
            assignment.setStandardDistanceLimit(request.getStandardDistanceLimit());
            assignment.setStandardTimeLimit(request.getStandardTimeLimit());
            assignment.setEmployeeExtraDistanceRate(request.getEmployeeExtraDistanceRate());
            assignment.setEmployeeExtraTimeRate(request.getEmployeeExtraTimeRate());
            assignment.setVendorExtraDistanceRate(request.getVendorExtraDistanceRate());
            assignment.setVendorExtraTimeRate(request.getVendorExtraTimeRate());
            assignment.setEstimatedVehiclesNeeded(request.getEstimatedVehiclesNeeded());
            
            ClientVendor savedAssignment = clientVendorRepository.save(assignment);
            return ResponseEntity.ok(savedAssignment);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Fix incorrect billing assignments
    @PostMapping("/fix-billing-assignments")
    public ResponseEntity<?> fixBillingAssignments() {
        try {
            List<ClientVendor> allAssignments = clientVendorRepository.findAll();
            int fixedCount = 0;
            
            for (ClientVendor assignment : allAssignments) {
                boolean needsUpdate = false;
                BillingModel billingModel = assignment.getBillingModel();
                
                switch (billingModel) {
                    case PACKAGE:
                        // PACKAGE model should have packageRate > 0 and tripRate = 0
                        if (assignment.getTripRate().compareTo(BigDecimal.ZERO) != 0) {
                            assignment.setTripRate(BigDecimal.ZERO);
                            needsUpdate = true;
                        }
                        break;
                    case TRIP:
                        // TRIP model should have tripRate > 0 and packageRate = 0
                        if (assignment.getPackageRate().compareTo(BigDecimal.ZERO) != 0) {
                            assignment.setPackageRate(BigDecimal.ZERO);
                            needsUpdate = true;
                        }
                        break;
                    case HYBRID:
                        // HYBRID model should have both rates > 0
                        // No automatic fix needed as both rates should be present
                        break;
                }
                
                if (needsUpdate) {
                    clientVendorRepository.save(assignment);
                    fixedCount++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Fixed billing assignments",
                "assignmentsFixed", fixedCount,
                "totalAssignmentsChecked", allAssignments.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Fix vendor rates for existing vendors
    @PostMapping("/fix-vendor-rates")
    public ResponseEntity<?> fixVendorRates() {
        try {
            List<VendorProfile> allVendors = vendorProfileRepository.findAll();
            int fixedCount = 0;
            
            for (VendorProfile vp : allVendors) {
                boolean needsUpdate = false;
                BillingModel billingModel = vp.getPreferredBillingModel() != null ? 
                    vp.getPreferredBillingModel() : vp.getBillingModel();
                
                if (billingModel != null) {
                    switch (billingModel) {
                        case PACKAGE:
                            if (vp.getDefaultPackageRate() == null) {
                                vp.setDefaultPackageRate(java.math.BigDecimal.valueOf(1000.0));
                                needsUpdate = true;
                            }
                            break;
                        case TRIP:
                            if (vp.getDefaultTripRate() == null) {
                                vp.setDefaultTripRate(java.math.BigDecimal.valueOf(50.0));
                                needsUpdate = true;
                            }
                            break;
                        case HYBRID:
                            if (vp.getDefaultPackageRate() == null) {
                                vp.setDefaultPackageRate(java.math.BigDecimal.valueOf(500.0));
                                needsUpdate = true;
                            }
                            if (vp.getDefaultTripRate() == null) {
                                vp.setDefaultTripRate(java.math.BigDecimal.valueOf(25.0));
                                needsUpdate = true;
                            }
                            break;
                    }
                    
                    // Set default overage rates if missing
                    if (vp.getDefaultExtraDistanceRate() == null) {
                        vp.setDefaultExtraDistanceRate(java.math.BigDecimal.valueOf(3.0));
                        needsUpdate = true;
                    }
                    if (vp.getDefaultExtraTimeRate() == null) {
                        vp.setDefaultExtraTimeRate(java.math.BigDecimal.valueOf(2.5));
                        needsUpdate = true;
                    }
                    
                    if (needsUpdate) {
                        vendorProfileRepository.save(vp);
                        fixedCount++;
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Fixed vendor rates",
                "vendorsFixed", fixedCount,
                "totalVendorsChecked", allVendors.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    

    // Get system analytics
    @GetMapping("/analytics")
    public ResponseEntity<?> getSystemAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            analytics.put("totalClients", userRepository.countByRole(UserRole.CLIENT));
            analytics.put("totalVendors", userRepository.countByRole(UserRole.VENDOR));
            analytics.put("totalEmployees", userRepository.countByRole(UserRole.EMPLOYEE));
            analytics.put("totalAssignments", clientVendorRepository.count());
            
            // Billing model distribution
            Map<String, Long> billingModelStats = new HashMap<>();
            for (BillingModel model : BillingModel.values()) {
                billingModelStats.put(model.toString(), 
                    clientVendorRepository.countByBillingModel(model));
            }
            analytics.put("billingModelDistribution", billingModelStats);
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get client-vendor assignments
    @GetMapping("/assignments")
    public ResponseEntity<?> getAllAssignments() {
        try {
            List<ClientVendor> assignments = clientVendorRepository.findAll();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    
    

    // Request DTOs
    public static class VendorProfileRequest {
        @NotBlank(message = "Billing model is required")
        private String billingModel;
        
        @NotNull(message = "Available vehicles is required")
        @Min(value = 1, message = "Available vehicles must be at least 1")
        private Integer availableVehicles;
        
        @NotNull(message = "Max client capacity is required")
        @Min(value = 1, message = "Max client capacity must be at least 1")
        private Integer maxClientCapacity;
        
        @NotNull(message = "Service quality rating is required")
        @DecimalMin(value = "0.0", message = "Service quality rating must be non-negative")
        @DecimalMin(value = "5.0", message = "Service quality rating cannot exceed 5.0")
        private java.math.BigDecimal serviceQualityRating;
        
        @NotBlank(message = "Geographic coverage is required")
        private String geographicCoverage;
        
        // Getters and setters
        public String getBillingModel() { return billingModel; }
        public void setBillingModel(String billingModel) { this.billingModel = billingModel; }
        public Integer getAvailableVehicles() { return availableVehicles; }
        public void setAvailableVehicles(Integer availableVehicles) { this.availableVehicles = availableVehicles; }
        public Integer getMaxClientCapacity() { return maxClientCapacity; }
        public void setMaxClientCapacity(Integer maxClientCapacity) { this.maxClientCapacity = maxClientCapacity; }
        public java.math.BigDecimal getServiceQualityRating() { return serviceQualityRating; }
        public void setServiceQualityRating(java.math.BigDecimal serviceQualityRating) { this.serviceQualityRating = serviceQualityRating; }
        public String getGeographicCoverage() { return geographicCoverage; }
        public void setGeographicCoverage(String geographicCoverage) { this.geographicCoverage = geographicCoverage; }
    }
    
    public static class VendorAssignmentRequest {
        @NotNull(message = "Client ID is required")
        @Positive(message = "Client ID must be positive")
        private Long clientId;
        
        @NotNull(message = "Vendor ID is required")
        @Positive(message = "Vendor ID must be positive")
        private Long vendorId;
        
        @NotBlank(message = "Billing model is required")
        private String billingModel;
        
        @NotNull(message = "Package rate is required")
        @DecimalMin(value = "0.0", message = "Package rate must be non-negative")
        private java.math.BigDecimal packageRate;
        
        @NotNull(message = "Trip rate is required")
        @DecimalMin(value = "0.0", message = "Trip rate must be non-negative")
        private java.math.BigDecimal tripRate;
        
        @NotNull(message = "Standard distance limit is required")
        @DecimalMin(value = "0.0", message = "Standard distance limit must be non-negative")
        private java.math.BigDecimal standardDistanceLimit;
        
        @NotNull(message = "Standard time limit is required")
        @DecimalMin(value = "0.0", message = "Standard time limit must be non-negative")
        private java.math.BigDecimal standardTimeLimit;
        
        @NotNull(message = "Employee extra distance rate is required")
        @DecimalMin(value = "0.0", message = "Employee extra distance rate must be non-negative")
        private java.math.BigDecimal employeeExtraDistanceRate;
        
        @NotNull(message = "Employee extra time rate is required")
        @DecimalMin(value = "0.0", message = "Employee extra time rate must be non-negative")
        private java.math.BigDecimal employeeExtraTimeRate;
        
        @NotNull(message = "Vendor extra distance rate is required")
        @DecimalMin(value = "0.0", message = "Vendor extra distance rate must be non-negative")
        private java.math.BigDecimal vendorExtraDistanceRate;
        
        @NotNull(message = "Vendor extra time rate is required")
        @DecimalMin(value = "0.0", message = "Vendor extra time rate must be non-negative")
        private java.math.BigDecimal vendorExtraTimeRate;
        
        @NotNull(message = "Estimated vehicles needed is required")
        @Min(value = 1, message = "Estimated vehicles needed must be at least 1")
        private Integer estimatedVehiclesNeeded;
        
        // Getters and setters
        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
        public String getBillingModel() { return billingModel; }
        public void setBillingModel(String billingModel) { this.billingModel = billingModel; }
        public java.math.BigDecimal getPackageRate() { return packageRate; }
        public void setPackageRate(java.math.BigDecimal packageRate) { this.packageRate = packageRate; }
        public java.math.BigDecimal getTripRate() { return tripRate; }
        public void setTripRate(java.math.BigDecimal tripRate) { this.tripRate = tripRate; }
        public java.math.BigDecimal getStandardDistanceLimit() { return standardDistanceLimit; }
        public void setStandardDistanceLimit(java.math.BigDecimal standardDistanceLimit) { this.standardDistanceLimit = standardDistanceLimit; }
        public java.math.BigDecimal getStandardTimeLimit() { return standardTimeLimit; }
        public void setStandardTimeLimit(java.math.BigDecimal standardTimeLimit) { this.standardTimeLimit = standardTimeLimit; }
        public java.math.BigDecimal getEmployeeExtraDistanceRate() { return employeeExtraDistanceRate; }
        public void setEmployeeExtraDistanceRate(java.math.BigDecimal employeeExtraDistanceRate) { this.employeeExtraDistanceRate = employeeExtraDistanceRate; }
        public java.math.BigDecimal getEmployeeExtraTimeRate() { return employeeExtraTimeRate; }
        public void setEmployeeExtraTimeRate(java.math.BigDecimal employeeExtraTimeRate) { this.employeeExtraTimeRate = employeeExtraTimeRate; }
        public java.math.BigDecimal getVendorExtraDistanceRate() { return vendorExtraDistanceRate; }
        public void setVendorExtraDistanceRate(java.math.BigDecimal vendorExtraDistanceRate) { this.vendorExtraDistanceRate = vendorExtraDistanceRate; }
        public java.math.BigDecimal getVendorExtraTimeRate() { return vendorExtraTimeRate; }
        public void setVendorExtraTimeRate(java.math.BigDecimal vendorExtraTimeRate) { this.vendorExtraTimeRate = vendorExtraTimeRate; }
        public Integer getEstimatedVehiclesNeeded() { return estimatedVehiclesNeeded; }
        public void setEstimatedVehiclesNeeded(Integer estimatedVehiclesNeeded) { this.estimatedVehiclesNeeded = estimatedVehiclesNeeded; }
    }
}