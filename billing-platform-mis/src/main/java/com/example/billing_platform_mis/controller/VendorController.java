package com.example.billing_platform_mis.controller;

import com.example.billing_platform_mis.entity.*;
import com.example.billing_platform_mis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/vendor")
@CrossOrigin(origins = "*")
public class VendorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientVendorRepository clientVendorRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private VendorProfileRepository vendorProfileRepository;

    

    // Get vendor profile
    @GetMapping("/profile")
    public ResponseEntity<?> getVendorProfile(@RequestParam Long vendorId) {
        try {
            User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

            if (vendor.getRole() != UserRole.VENDOR) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is not a vendor"));
            }

            Optional<VendorProfile> profile = vendorProfileRepository.findByUserId(vendorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", vendor.getId());
            response.put("name", vendor.getName());
            response.put("email", vendor.getEmail());
            response.put("role", vendor.getRole().toString());
            
            if (profile.isPresent()) {
                VendorProfile vp = profile.get();
                response.put("maxClientCapacity", vp.getMaxClientCapacity());
                response.put("currentLoad", clientVendorRepository.countClientsByVendorId(vendorId));
                response.put("availableCapacity", vp.getMaxClientCapacity() - clientVendorRepository.countClientsByVendorId(vendorId));
            } else {
                response.put("maxClientCapacity", 0);
                response.put("currentLoad", 0);
                response.put("availableCapacity", 0);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/clients")
    public ResponseEntity<?> getAssignedClients(@RequestParam Long vendorId) {
        try {
            User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

            List<ClientVendor> assignments = clientVendorRepository.findByVendorIdWithClientDetails(vendorId);
            
            List<Map<String, Object>> clients = assignments.stream()
                .map(cv -> {
                    Map<String, Object> client = new HashMap<>();
                    client.put("clientId", cv.getClient().getId());
                    client.put("clientName", cv.getClient().getName());
                    client.put("clientEmail", cv.getClient().getEmail());
                    client.put("billingModel", cv.getBillingModel().toString());
                    client.put("packageRate", cv.getPackageRate());
                    client.put("tripRate", cv.getTripRate());
                    client.put("assignedDate", cv.getCreatedAt());
                    return client;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/trips")
    public ResponseEntity<?> getVendorTrips(@RequestParam Long vendorId,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate) {
        try {
            List<Trip> trips;
            
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                trips = tripRepository.findByVendorIdAndDateRange(vendorId, start, end);
            } else {
                trips = tripRepository.findByVendorId(vendorId);
            }

            // Format trips for frontend consumption
            List<Map<String, Object>> formattedTrips = trips.stream()
                .map(trip -> {
                    Map<String, Object> tripData = new HashMap<>();
                    tripData.put("id", trip.getId());
                    tripData.put("clientName", trip.getClientVendor().getClient().getName());
                    tripData.put("employeeName", trip.getEmployee().getName());
                    tripData.put("distance", trip.getDistance());
                    tripData.put("duration", trip.getDuration());
                    tripData.put("date", trip.getTripDate().toString());
                    tripData.put("createdAt", trip.getCreatedAt());
                    tripData.put("status", trip.getStatus() != null ? trip.getStatus().toString() : "COMPLETED");
                    
                    // Include cost information
                    tripData.put("totalCost", trip.getTotalCost() != null ? trip.getTotalCost() : BigDecimal.ZERO);
                    tripData.put("vendorCost", trip.getVendorCost() != null ? trip.getVendorCost() : BigDecimal.ZERO);
                    tripData.put("employeeCost", trip.getEmployeeCost() != null ? trip.getEmployeeCost() : BigDecimal.ZERO);
                    tripData.put("extraDistanceCost", trip.getExtraDistanceCost() != null ? trip.getExtraDistanceCost() : BigDecimal.ZERO);
                    tripData.put("extraTimeCost", trip.getExtraTimeCost() != null ? trip.getExtraTimeCost() : BigDecimal.ZERO);
                    
                    // For frontend compatibility
                    tripData.put("amount", trip.getVendorCost() != null ? trip.getVendorCost() : BigDecimal.ZERO);
                    tripData.put("destination", trip.getDistance() + " km, " + trip.getDuration() + " min");
                    
                    return tripData;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(formattedTrips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get trips by client
    @GetMapping("/trips/client/{clientId}")
    public ResponseEntity<?> getTripsForClient(@PathVariable Long clientId, @RequestParam Long vendorId) {
        try {
            List<Trip> trips = tripRepository.findByVendorIdAndClientId(vendorId, clientId);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/payouts")
    public ResponseEntity<?> getPayoutHistory(@RequestParam Long vendorId,
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate) {
        try {
            List<ClientVendor> assignments = clientVendorRepository.findByVendorId(vendorId);
            List<Payout> allPayouts = new ArrayList<>();

            for (ClientVendor cv : assignments) {
                List<Payout> payouts;
                if (startDate != null && endDate != null) {
                    LocalDate start = LocalDate.parse(startDate);
                    LocalDate end = LocalDate.parse(endDate);
                    payouts = payoutRepository.findByClientVendorAndDateRange(cv.getId(), start, end);
                } else {
                    payouts = payoutRepository.findByClientVendor(cv);
                }
                allPayouts.addAll(payouts);
            }

            return ResponseEntity.ok(allPayouts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get payout summary
    @GetMapping("/payouts/summary")
    public ResponseEntity<?> getPayoutSummary(@RequestParam Long vendorId) {
        try {
            List<ClientVendor> assignments = clientVendorRepository.findByVendorId(vendorId);
            
            Map<String, Object> summary = new HashMap<>();
            BigDecimal totalPayouts = BigDecimal.ZERO;
            int totalPayoutCount = 0;
            
            List<Map<String, Object>> clientBreakdown = new ArrayList<>();

            for (ClientVendor cv : assignments) {
                List<Payout> payouts = payoutRepository.findByClientVendor(cv);
                BigDecimal clientTotal = payouts.stream()
                    .map(Payout::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                totalPayouts = totalPayouts.add(clientTotal);
                totalPayoutCount += payouts.size();

                Map<String, Object> clientSummary = new HashMap<>();
                clientSummary.put("clientName", cv.getClient().getName());
                clientSummary.put("billingModel", cv.getBillingModel().toString());
                clientSummary.put("totalPayouts", clientTotal);
                clientSummary.put("payoutCount", payouts.size());
                clientBreakdown.add(clientSummary);
            }

            summary.put("totalPayouts", totalPayouts);
            summary.put("totalPayoutCount", totalPayoutCount);
            summary.put("activeClients", assignments.size());
            summary.put("clientBreakdown", clientBreakdown);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/reports/performance")
    public ResponseEntity<?> getPerformanceSummary(@RequestParam Long vendorId) {
        try {
            List<Trip> allTrips = tripRepository.findByVendorId(vendorId);
            List<ClientVendor> assignments = clientVendorRepository.findByVendorId(vendorId);
            
            Map<String, Object> performance = new HashMap<>();
            
            // Trip statistics
            performance.put("totalTrips", allTrips.size());
            performance.put("totalDistance", allTrips.stream()
                .mapToDouble(t -> t.getDistance().doubleValue()).sum());
            performance.put("totalDuration", allTrips.stream()
                .mapToDouble(t -> t.getDuration().doubleValue()).sum());
            performance.put("averageDistance", allTrips.isEmpty() ? 0 :
                allTrips.stream().mapToDouble(t -> t.getDistance().doubleValue()).average().orElse(0));
            performance.put("averageDuration", allTrips.isEmpty() ? 0 :
                allTrips.stream().mapToDouble(t -> t.getDuration().doubleValue()).average().orElse(0));
            
            // Earnings statistics
            BigDecimal totalEarnings = allTrips.stream()
                .map(t -> t.getVendorCost() != null ? t.getVendorCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            performance.put("totalEarnings", totalEarnings);
            performance.put("averageEarningsPerTrip", allTrips.isEmpty() ? BigDecimal.ZERO :
                totalEarnings.divide(BigDecimal.valueOf(allTrips.size()), 2, BigDecimal.ROUND_HALF_UP));
            
            // Monthly earnings (current month)
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate monthEnd = LocalDate.now();
            List<Trip> monthlyTrips = tripRepository.findByVendorIdAndDateRange(vendorId, monthStart, monthEnd);
            BigDecimal monthlyEarnings = monthlyTrips.stream()
                .map(t -> t.getVendorCost() != null ? t.getVendorCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            performance.put("monthlyEarnings", monthlyEarnings);

            // Client statistics
            performance.put("activeClients", assignments.size());
            
            // Capacity utilization
            Optional<VendorProfile> profile = vendorProfileRepository.findByUserId(vendorId);
            if (profile.isPresent()) {
                int maxCapacity = profile.get().getMaxClientCapacity();
                int currentLoad = assignments.size();
                performance.put("capacityUtilization", maxCapacity > 0 ? 
                    (double) currentLoad / maxCapacity * 100 : 0);
                performance.put("maxCapacity", maxCapacity);
                performance.put("currentLoad", currentLoad);
            }

            // Recent activity (last 30 days)
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<Trip> recentTrips = tripRepository.findByVendorIdAndDateRange(vendorId, thirtyDaysAgo, LocalDate.now());
            performance.put("recentTrips", recentTrips.size());

            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get monthly summary
    @GetMapping("/reports/monthly")
    public ResponseEntity<?> getMonthlySummary(@RequestParam Long vendorId,
                                             @RequestParam(required = false) String month) {
        try {
            LocalDate startDate, endDate;
            
            if (month != null) {
                // Parse month in format "2024-01"
                String[] parts = month.split("-");
                int year = Integer.parseInt(parts[0]);
                int monthNum = Integer.parseInt(parts[1]);
                startDate = LocalDate.of(year, monthNum, 1);
                endDate = startDate.plusMonths(1).minusDays(1);
            } else {
                // Current month
                LocalDate now = LocalDate.now();
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
            }

            List<Trip> monthlyTrips = tripRepository.findByVendorIdAndDateRange(vendorId, startDate, endDate);
            List<ClientVendor> assignments = clientVendorRepository.findByVendorId(vendorId);
            
            Map<String, Object> monthlySummary = new HashMap<>();
            monthlySummary.put("period", startDate.toString() + " to " + endDate.toString());
            monthlySummary.put("totalTrips", monthlyTrips.size());
            monthlySummary.put("totalDistance", monthlyTrips.stream()
                .mapToDouble(t -> t.getDistance().doubleValue()).sum());
            
            // Calculate monthly payouts
            BigDecimal monthlyPayouts = BigDecimal.ZERO;
            for (ClientVendor cv : assignments) {
                List<Payout> payouts = payoutRepository.findByClientVendorAndDateRange(cv.getId(), startDate, endDate);
                monthlyPayouts = monthlyPayouts.add(
                    payouts.stream()
                        .map(Payout::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
            }
            monthlySummary.put("monthlyPayouts", monthlyPayouts);

            return ResponseEntity.ok(monthlySummary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @PutMapping("/rates")
    public ResponseEntity<?> updateVendorRates(@RequestBody VendorRateUpdateRequest request) {
        try {
            User vendor = userRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

            if (vendor.getRole() != UserRole.VENDOR) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is not a vendor"));
            }

            // Get or create vendor profile
            VendorProfile profile = vendorProfileRepository.findByUserId(request.getVendorId())
                .orElse(new VendorProfile());
            
            if (profile.getUser() == null) {
                profile.setUser(vendor);
                profile.setMaxClientCapacity(10); // Default capacity
            }

            // Update rates in vendor profile
            BillingModel billingModel = BillingModel.valueOf(request.getBillingModel().toUpperCase());
            profile.setBillingModel(billingModel); // Set the required billing model field
            profile.setPreferredBillingModel(billingModel);
            profile.setDefaultPackageRate(request.getPackageRate());
            profile.setDefaultTripRate(request.getTripRate());
            profile.setDefaultExtraDistanceRate(request.getExtraDistanceRate());
            profile.setDefaultExtraTimeRate(request.getExtraTimeRate());

            vendorProfileRepository.save(profile);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vendor rates updated successfully");
            response.put("billingModel", profile.getPreferredBillingModel().toString());
            response.put("packageRate", profile.getDefaultPackageRate());
            response.put("tripRate", profile.getDefaultTripRate());
            response.put("extraDistanceRate", profile.getDefaultExtraDistanceRate());
            response.put("extraTimeRate", profile.getDefaultExtraTimeRate());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get vendor's current rates
    @GetMapping("/rates")
    public ResponseEntity<?> getVendorRates(@RequestParam Long vendorId) {
        try {
            User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

            if (vendor.getRole() != UserRole.VENDOR) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is not a vendor"));
            }

            Optional<VendorProfile> profile = vendorProfileRepository.findByUserId(vendorId);
            
            Map<String, Object> rates = new HashMap<>();
            if (profile.isPresent()) {
                VendorProfile vp = profile.get();
                rates.put("billingModel", vp.getPreferredBillingModel() != null ? 
                    vp.getPreferredBillingModel().toString() : "PACKAGE");
                rates.put("packageRate", vp.getDefaultPackageRate() != null ? 
                    vp.getDefaultPackageRate() : BigDecimal.valueOf(100));
                rates.put("tripRate", vp.getDefaultTripRate() != null ? 
                    vp.getDefaultTripRate() : BigDecimal.valueOf(15));
                rates.put("extraDistanceRate", vp.getDefaultExtraDistanceRate() != null ? 
                    vp.getDefaultExtraDistanceRate() : BigDecimal.valueOf(3));
                rates.put("extraTimeRate", vp.getDefaultExtraTimeRate() != null ? 
                    vp.getDefaultExtraTimeRate() : BigDecimal.valueOf(2.5));
            } else {
                // Default rates for new vendors
                rates.put("billingModel", "PACKAGE");
                rates.put("packageRate", BigDecimal.valueOf(100));
                rates.put("tripRate", BigDecimal.valueOf(15));
                rates.put("extraDistanceRate", BigDecimal.valueOf(3));
                rates.put("extraTimeRate", BigDecimal.valueOf(2.5));
            }

            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    public static class VendorRateUpdateRequest {
        private Long vendorId;
        private String billingModel;
        private BigDecimal packageRate;
        private BigDecimal tripRate;
        private BigDecimal extraDistanceRate;
        private BigDecimal extraTimeRate;

        // Getters and setters
        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
        
        public String getBillingModel() { return billingModel; }
        public void setBillingModel(String billingModel) { this.billingModel = billingModel; }
        
        public BigDecimal getPackageRate() { return packageRate; }
        public void setPackageRate(BigDecimal packageRate) { this.packageRate = packageRate; }
        
        public BigDecimal getTripRate() { return tripRate; }
        public void setTripRate(BigDecimal tripRate) { this.tripRate = tripRate; }
        
        public BigDecimal getExtraDistanceRate() { return extraDistanceRate; }
        public void setExtraDistanceRate(BigDecimal extraDistanceRate) { this.extraDistanceRate = extraDistanceRate; }
        
        public BigDecimal getExtraTimeRate() { return extraTimeRate; }
        public void setExtraTimeRate(BigDecimal extraTimeRate) { this.extraTimeRate = extraTimeRate; }
    }
}