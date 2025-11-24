package com.example.billing_platform_mis.controller;

import com.example.billing_platform_mis.entity.*;
import com.example.billing_platform_mis.service.UserService;
import com.example.billing_platform_mis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*")
public class ClientController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClientEmployeeRepository clientEmployeeRepository;
    
    @Autowired
    private ClientVendorRepository clientVendorRepository;
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private IncentiveRepository incentiveRepository;
    
    @GetMapping("/employees")
    public ResponseEntity<?> getEmployees(@RequestParam @Positive(message = "Client ID must be positive") Long clientId) {
        try {
            List<ClientEmployee> clientEmployees = clientEmployeeRepository.findByClientId(clientId);
            return ResponseEntity.ok(clientEmployees);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Add new employee to client
    @PostMapping("/employees")
    public ResponseEntity<?> addEmployee(@Valid @RequestBody AddEmployeeRequest request) {
        try {
            User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + request.getClientId()));
            
            User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmployeeId()));
            
            
            if (client.getRole() != UserRole.CLIENT) {
                return ResponseEntity.badRequest().body(Map.of("error", "User with ID " + request.getClientId() + " is not a CLIENT, found role: " + client.getRole()));
            }
            if (employee.getRole() != UserRole.EMPLOYEE) {
                return ResponseEntity.badRequest().body(Map.of("error", "User with ID " + request.getEmployeeId() + " is not an EMPLOYEE, found role: " + employee.getRole()));
            }
            

            if (clientEmployeeRepository.existsByClientIdAndEmployeeId(request.getClientId(), request.getEmployeeId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employee is already assigned to this client"));
            }
            
            ClientEmployee clientEmployee = new ClientEmployee();
            clientEmployee.setClient(client);
            clientEmployee.setEmployee(employee);
            
            ClientEmployee saved = clientEmployeeRepository.save(clientEmployee);
            

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("clientId", saved.getClient().getId());
            response.put("clientName", saved.getClient().getName());
            response.put("employeeId", saved.getEmployee().getId());
            response.put("employeeName", saved.getEmployee().getName());
            response.put("createdAt", saved.getCreatedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in addEmployee: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "details", e.getClass().getSimpleName()));
        }
    }
    
    // Remove employee from client
    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<?> removeEmployee(@PathVariable Long employeeId, @RequestParam Long clientId) {
        try {
            ClientEmployee clientEmployee = clientEmployeeRepository
                .findByClientIdAndEmployeeId(clientId, employeeId)
                .orElseThrow(() -> new RuntimeException("Employee assignment not found"));
            
            clientEmployeeRepository.delete(clientEmployee);
            return ResponseEntity.ok(Map.of("message", "Employee removed successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/trips")
    public ResponseEntity<?> getAllTrips(@RequestParam Long clientId) {
        try {
            List<Trip> trips = tripRepository.findTripsByClientId(clientId);
            
            
            List<Map<String, Object>> formattedTrips = trips.stream()
                .map(trip -> {
                    Map<String, Object> tripData = new HashMap<>();
                    tripData.put("id", trip.getId());
                    tripData.put("employeeName", trip.getEmployee().getName());
                    tripData.put("vendorName", trip.getClientVendor().getVendor().getName());
                    tripData.put("distance", trip.getDistance());
                    tripData.put("duration", trip.getDuration());
                    tripData.put("date", trip.getTripDate().toString());
                    tripData.put("tripDate", trip.getTripDate().toString());
                    tripData.put("createdAt", trip.getCreatedAt());
                    tripData.put("status", trip.getStatus() != null ? trip.getStatus().toString() : "COMPLETED");
                    
                 
                    tripData.put("totalCost", trip.getTotalCost() != null ? trip.getTotalCost() : BigDecimal.ZERO);
                    tripData.put("amount", trip.getTotalCost() != null ? trip.getTotalCost() : BigDecimal.ZERO); // Frontend compatibility
                    tripData.put("employeeCost", trip.getEmployeeCost() != null ? trip.getEmployeeCost() : BigDecimal.ZERO);
                    tripData.put("vendorCost", trip.getVendorCost() != null ? trip.getVendorCost() : BigDecimal.ZERO);
                    tripData.put("extraDistanceCost", trip.getExtraDistanceCost() != null ? trip.getExtraDistanceCost() : BigDecimal.ZERO);
                    tripData.put("extraTimeCost", trip.getExtraTimeCost() != null ? trip.getExtraTimeCost() : BigDecimal.ZERO);
                    
                    
                    tripData.put("destination", trip.getDistance() + " km, " + trip.getDuration() + " min");
                    
                    
                    tripData.put("billingModel", trip.getClientVendor().getBillingModel().toString());
                    
                    return tripData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(formattedTrips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // View trips for specific employee
    @GetMapping("/trips/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeTrips(@PathVariable Long employeeId, @RequestParam Long clientId) {
        try {
            List<Trip> trips = tripRepository.findByEmployeeIdAndClientId(employeeId, clientId);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // View trips by specific vendor
    @GetMapping("/trips/vendor/{vendorId}")
    public ResponseEntity<?> getVendorTrips(@PathVariable Long vendorId, @RequestParam Long clientId) {
        try {
            List<Trip> trips = tripRepository.findByVendorIdAndClientId(vendorId, clientId);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Filter trips by date range
    @GetMapping("/trips/date-range")
    public ResponseEntity<?> getTripsByDateRange(
            @RequestParam Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Trip> trips = tripRepository.findByClientIdAndDateRange(clientId, startDate, endDate);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/vendors")
    public ResponseEntity<?> getAssignedVendors(@RequestParam Long clientId) {
        try {
            List<ClientVendor> assignments = clientVendorRepository.findByClientId(clientId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Vendor performance metrics
    @GetMapping("/vendors/{vendorId}/performance")
    public ResponseEntity<?> getVendorPerformance(@PathVariable Long vendorId, @RequestParam Long clientId) {
        try {
            List<Trip> vendorTrips = tripRepository.findByVendorIdAndClientId(vendorId, clientId);
            
            Map<String, Object> performance = new HashMap<>();
            performance.put("totalTrips", vendorTrips.size());
            performance.put("totalDistance", vendorTrips.stream()
                .mapToDouble(trip -> trip.getDistance().doubleValue()).sum());
            performance.put("totalDuration", vendorTrips.stream()
                .mapToDouble(trip -> trip.getDuration().doubleValue()).sum());
            performance.put("averageDistance", vendorTrips.isEmpty() ? 0 : 
                vendorTrips.stream().mapToDouble(trip -> trip.getDistance().doubleValue()).average().orElse(0));
            
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    

    @GetMapping("/vendors/{vendorId}/trips")
    public ResponseEntity<?> getVendorAllTrips(@PathVariable Long vendorId, @RequestParam Long clientId) {
        try {
            List<Trip> trips = tripRepository.findByVendorIdAndClientId(vendorId, clientId);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardSummary(@RequestParam Long clientId) {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // Employee count
            long employeeCount = clientEmployeeRepository.countByClientId(clientId);
            summary.put("totalEmployees", employeeCount);
            
            // Vendor count
            long vendorCount = clientVendorRepository.countByClientId(clientId);
            summary.put("assignedVendors", vendorCount);
            
            // Trip statistics
            List<Trip> allTrips = tripRepository.findTripsByClientId(clientId);
            summary.put("totalTrips", allTrips.size());
            summary.put("totalDistance", allTrips.stream()
                .mapToDouble(trip -> trip.getDistance().doubleValue()).sum());
            
            // Recent trips (last 30 days)
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<Trip> recentTrips = tripRepository.findByClientIdAndDateRange(clientId, thirtyDaysAgo, LocalDate.now());
            summary.put("recentTrips", recentTrips.size());
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Employee incentive report
    @GetMapping("/reports/employee-incentives")
    public ResponseEntity<?> getEmployeeIncentiveReport(@RequestParam Long clientId) {
        try {
            List<Incentive> incentives = incentiveRepository.findByClientId(clientId);
            
            Map<Long, Map<String, Object>> employeeIncentives = incentives.stream()
                .collect(Collectors.groupingBy(
                    incentive -> incentive.getTrip().getEmployee().getId(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("employeeName", list.get(0).getTrip().getEmployee().getName());
                            stats.put("totalIncentives", list.stream()
                                .mapToDouble(i -> i.getTotalAmount().doubleValue()).sum());
                            stats.put("incentiveCount", list.size());
                            return stats;
                        }
                    )
                ));
            
            return ResponseEntity.ok(employeeIncentives);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Vendor cost breakdown
    @GetMapping("/reports/vendor-costs")
    public ResponseEntity<?> getVendorCostReport(@RequestParam Long clientId) {
        try {
            List<ClientVendor> assignments = clientVendorRepository.findByClientId(clientId);
            
            List<Map<String, Object>> vendorCosts = assignments.stream()
                .map(assignment -> {
                    Map<String, Object> cost = new HashMap<>();
                    cost.put("vendorName", assignment.getVendor().getName());
                    cost.put("billingModel", assignment.getBillingModel().toString());
                    cost.put("packageRate", assignment.getPackageRate());
                    cost.put("tripRate", assignment.getTripRate());
                    
                    // Calculate trip count for this vendor
                    List<Trip> vendorTrips = tripRepository.findByVendorIdAndClientId(
                        assignment.getVendor().getId(), clientId);
                    cost.put("totalTrips", vendorTrips.size());
                    
                    return cost;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(vendorCosts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/billing/summary")
    public ResponseEntity<?> getBillingSummary(@RequestParam Long clientId) {
        try {
            List<ClientVendor> assignments = clientVendorRepository.findByClientId(clientId);
            
            Map<String, Object> billingSummary = new HashMap<>();
            double totalPackageCosts = assignments.stream()
                .filter(a -> a.getBillingModel() == BillingModel.PACKAGE || a.getBillingModel() == BillingModel.HYBRID)
                .mapToDouble(a -> a.getPackageRate() != null ? a.getPackageRate().doubleValue() : 0)
                .sum();
            
            billingSummary.put("monthlyPackageCosts", totalPackageCosts);
            billingSummary.put("activeVendors", assignments.size());
            billingSummary.put("billingModels", assignments.stream()
                .collect(Collectors.groupingBy(
                    a -> a.getBillingModel().toString(),
                    Collectors.counting()
                )));
            
            return ResponseEntity.ok(billingSummary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
   
    @GetMapping("/incentives")
    public ResponseEntity<?> getIncentiveSummaries(@RequestParam Long clientId) {
        try {
            List<Incentive> incentives = incentiveRepository.findByClientId(clientId);
            return ResponseEntity.ok(incentives);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/trips/book")
    public ResponseEntity<?> bookTrip(@Valid @RequestBody BookTripRequest request) {
        try {

            User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
            
            if (client.getRole() != UserRole.CLIENT) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is not a client"));
            }
            

            if (!clientEmployeeRepository.existsByClientIdAndEmployeeId(request.getClientId(), request.getEmployeeId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employee is not associated with this client"));
            }
            
            // Find client-vendor relationship
            ClientVendor clientVendor = clientVendorRepository.findByClientIdAndVendorId(request.getClientId(), request.getVendorId())
                .orElseThrow(() -> new RuntimeException("No agreement exists between client and vendor"));
            

            User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            

            Trip trip = new Trip();
            trip.setClientVendor(clientVendor);
            trip.setEmployee(employee);
            trip.setDistance(request.getDistance());
            trip.setDuration(request.getDuration());
            trip.setTripDate(request.getTripDate());
            trip.setStatus(TripStatus.COMPLETED);
            
            // Calculate billing costs
            calculateTripCosts(trip, clientVendor);
            
            Trip savedTrip = tripRepository.save(trip);
            

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTrip.getId());
            response.put("employeeName", savedTrip.getEmployee().getName());
            response.put("vendorName", savedTrip.getClientVendor().getVendor().getName());
            response.put("distance", savedTrip.getDistance());
            response.put("duration", savedTrip.getDuration());
            response.put("tripDate", savedTrip.getTripDate());
            response.put("date", savedTrip.getTripDate().toString()); // Frontend compatibility
            response.put("totalCost", savedTrip.getTotalCost());
            response.put("amount", savedTrip.getTotalCost()); // Frontend compatibility
            response.put("employeeCost", savedTrip.getEmployeeCost());
            response.put("vendorCost", savedTrip.getVendorCost());
            response.put("extraDistanceCost", savedTrip.getExtraDistanceCost());
            response.put("extraTimeCost", savedTrip.getExtraTimeCost());
            response.put("status", savedTrip.getStatus());
            response.put("destination", savedTrip.getDistance() + " km, " + savedTrip.getDuration() + " min");
            response.put("createdAt", savedTrip.getCreatedAt());
            response.put("message", "Trip booked successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    

    

    
    //REQUEST DTOs
    
    public static class AddEmployeeRequest {
        @NotNull(message = "Client ID is required")
        @Positive(message = "Client ID must be positive")
        private Long clientId;
        
        @NotNull(message = "Employee ID is required")
        @Positive(message = "Employee ID must be positive")
        private Long employeeId;
        
        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    }
    
    public static class BookTripRequest {
        @NotNull(message = "Client ID is required")
        private Long clientId;
        
        @NotNull(message = "Employee ID is required")
        private Long employeeId;
        
        @NotNull(message = "Vendor ID is required")
        private Long vendorId;
        
        @NotNull(message = "Distance is required")
        private BigDecimal distance;
        
        @NotNull(message = "Duration is required")
        private BigDecimal duration;
        
        @NotNull(message = "Trip date is required")
        private LocalDate tripDate;
        

        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
        public BigDecimal getDistance() { return distance; }
        public void setDistance(BigDecimal distance) { this.distance = distance; }
        public BigDecimal getDuration() { return duration; }
        public void setDuration(BigDecimal duration) { this.duration = duration; }
        public LocalDate getTripDate() { return tripDate; }
        public void setTripDate(LocalDate tripDate) { this.tripDate = tripDate; }
    }
    
  
    
    private void calculateTripCosts(Trip trip, ClientVendor clientVendor) {
        BillingModel billingModel = clientVendor.getBillingModel();
        
        
        BigDecimal baseCost = BigDecimal.ZERO;
        if (billingModel == BillingModel.TRIP || billingModel == BillingModel.HYBRID) {
            baseCost = clientVendor.getTripRate() != null ? clientVendor.getTripRate() : BigDecimal.ZERO;
        }
        
        if (billingModel == BillingModel.PACKAGE || billingModel == BillingModel.HYBRID) {
        
            BigDecimal packageContribution = clientVendor.getPackageRate() != null ? 
                clientVendor.getPackageRate().divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
            baseCost = baseCost.add(packageContribution);
        }
        
        // Calculate extra distance and time amounts
        BigDecimal extraDistance = BigDecimal.ZERO;
        BigDecimal extraTime = BigDecimal.ZERO;
        
        if (clientVendor.getStandardDistanceLimit() != null && 
            trip.getDistance().compareTo(clientVendor.getStandardDistanceLimit()) > 0) {
            extraDistance = trip.getDistance().subtract(clientVendor.getStandardDistanceLimit());
        }
        
        if (clientVendor.getStandardTimeLimit() != null && 
            trip.getDuration().compareTo(clientVendor.getStandardTimeLimit()) > 0) {
            extraTime = trip.getDuration().subtract(clientVendor.getStandardTimeLimit());
        }
        
        // Calculate VENDOR costs 
        BigDecimal vendorExtraDistanceCost = BigDecimal.ZERO;
        BigDecimal vendorExtraTimeCost = BigDecimal.ZERO;
        
        if (extraDistance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal vendorExtraDistanceRate = clientVendor.getVendorExtraDistanceRate() != null ? 
                clientVendor.getVendorExtraDistanceRate() : BigDecimal.ZERO;
            vendorExtraDistanceCost = extraDistance.multiply(vendorExtraDistanceRate);
        }
        
        if (extraTime.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal vendorExtraTimeRate = clientVendor.getVendorExtraTimeRate() != null ? 
                clientVendor.getVendorExtraTimeRate() : BigDecimal.ZERO;
            vendorExtraTimeCost = extraTime.multiply(vendorExtraTimeRate);
        }
        
   
        BigDecimal employeeExtraDistanceCost = BigDecimal.ZERO;
        BigDecimal employeeExtraTimeCost = BigDecimal.ZERO;
        
        if (extraDistance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal employeeExtraDistanceRate = clientVendor.getEmployeeExtraDistanceRate() != null ? 
                clientVendor.getEmployeeExtraDistanceRate() : BigDecimal.ZERO;
            employeeExtraDistanceCost = extraDistance.multiply(employeeExtraDistanceRate);
        }
        
        if (extraTime.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal employeeExtraTimeRate = clientVendor.getEmployeeExtraTimeRate() != null ? 
                clientVendor.getEmployeeExtraTimeRate() : BigDecimal.ZERO;
            employeeExtraTimeCost = extraTime.multiply(employeeExtraTimeRate);
        }
        
        
        BigDecimal halfBaseCost = baseCost.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
        
   
        BigDecimal vendorTotalCost = halfBaseCost.add(vendorExtraDistanceCost).add(vendorExtraTimeCost);
        BigDecimal employeeTotalCost = halfBaseCost.add(employeeExtraDistanceCost).add(employeeExtraTimeCost);
        
        // Set trip costs
        trip.setVendorCost(vendorTotalCost);
        trip.setEmployeeCost(employeeTotalCost);
        trip.setTotalCost(vendorTotalCost.add(employeeTotalCost));
        
        // Store extra costs
        trip.setExtraDistanceCost(employeeExtraDistanceCost);
        trip.setExtraTimeCost(employeeExtraTimeCost);
    }
}