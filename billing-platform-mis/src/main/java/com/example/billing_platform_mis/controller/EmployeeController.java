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
@RequestMapping("/api/employee")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientEmployeeRepository clientEmployeeRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private IncentiveRepository incentiveRepository;

    
    @GetMapping("/profile")
    public ResponseEntity<?> getEmployeeProfile(@RequestParam Long employeeId) {
        try {
            User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            if (employee.getRole() != UserRole.EMPLOYEE) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is not an employee"));
            }

            // Get client assignments
            List<ClientEmployee> assignments = clientEmployeeRepository.findByEmployeeId(employeeId);
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", employee.getId());
            profile.put("name", employee.getName());
            profile.put("email", employee.getEmail());
            profile.put("role", employee.getRole().toString());
            profile.put("assignedClients", assignments.size());
            
            // Add client details
            List<Map<String, Object>> clients = assignments.stream()
                .map(ce -> {
                    Map<String, Object> client = new HashMap<>();
                    client.put("clientId", ce.getClient().getId());
                    client.put("clientName", ce.getClient().getName());
                    client.put("assignedDate", ce.getCreatedAt());
                    return client;
                })
                .collect(Collectors.toList());
            profile.put("clients", clients);

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/trips")
    public ResponseEntity<?> getEmployeeTrips(@RequestParam Long employeeId,
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate,
                                            @RequestParam(required = false) Long clientId) {
        try {
            User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            List<Trip> trips;
            
            if (clientId != null) {
                // Filter by specific client
                trips = tripRepository.findByEmployeeIdAndClientId(employeeId, clientId);
            } else if (startDate != null && endDate != null) {
                // Filter by date range
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                trips = tripRepository.findByEmployeeIdAndDateRange(employeeId, start, end);
            } else {
                // All trips
                trips = tripRepository.findByEmployeeId(employeeId);
            }

            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get trip summary
    @GetMapping("/trips/summary")
    public ResponseEntity<?> getTripSummary(@RequestParam Long employeeId) {
        try {
            List<Trip> allTrips = tripRepository.findByEmployeeId(employeeId);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalTrips", allTrips.size());
            summary.put("totalDistance", allTrips.stream()
                .mapToDouble(t -> t.getDistance().doubleValue()).sum());
            summary.put("totalDuration", allTrips.stream()
                .mapToDouble(t -> t.getDuration().doubleValue()).sum());
            summary.put("averageDistance", allTrips.isEmpty() ? 0 :
                allTrips.stream().mapToDouble(t -> t.getDistance().doubleValue()).average().orElse(0));
            summary.put("averageDuration", allTrips.isEmpty() ? 0 :
                allTrips.stream().mapToDouble(t -> t.getDuration().doubleValue()).average().orElse(0));

            // Recent trips (last 30 days)
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<Trip> recentTrips = tripRepository.findByEmployeeIdAndDateRange(employeeId, thirtyDaysAgo, LocalDate.now());
            summary.put("recentTrips", recentTrips.size());

            // Group by client
            Map<String, Long> tripsByClient = allTrips.stream()
                .collect(Collectors.groupingBy(
                    trip -> trip.getClientVendor().getClient().getName(),
                    Collectors.counting()
                ));
            summary.put("tripsByClient", tripsByClient);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/incentives")
    public ResponseEntity<?> getEmployeeIncentives(@RequestParam Long employeeId,
                                                 @RequestParam(required = false) String startDate,
                                                 @RequestParam(required = false) String endDate) {
        try {
            List<Incentive> incentives;
            
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                incentives = incentiveRepository.findByEmployeeIdAndDateRange(employeeId, start, end);
            } else {
                incentives = incentiveRepository.findByEmployeeId(employeeId);
            }

            return ResponseEntity.ok(incentives);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get incentive summary
    @GetMapping("/incentives/summary")
    public ResponseEntity<?> getIncentiveSummary(@RequestParam Long employeeId) {
        try {
            List<Incentive> allIncentives = incentiveRepository.findByEmployeeId(employeeId);
            
            Map<String, Object> summary = new HashMap<>();
            
            BigDecimal totalIncentives = allIncentives.stream()
                .map(Incentive::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalDistanceIncentives = allIncentives.stream()
                .map(Incentive::getDistanceIncentive)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalTimeIncentives = allIncentives.stream()
                .map(Incentive::getTimeIncentive)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            summary.put("totalIncentives", totalIncentives);
            summary.put("totalDistanceIncentives", totalDistanceIncentives);
            summary.put("totalTimeIncentives", totalTimeIncentives);
            summary.put("incentiveCount", allIncentives.size());
            summary.put("averageIncentive", allIncentives.isEmpty() ? BigDecimal.ZERO :
                totalIncentives.divide(BigDecimal.valueOf(allIncentives.size()), 2, BigDecimal.ROUND_HALF_UP));

            // Recent incentives (last 30 days)
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<Incentive> recentIncentives = incentiveRepository.findByEmployeeIdAndDateRange(employeeId, thirtyDaysAgo, LocalDate.now());
            BigDecimal recentTotal = recentIncentives.stream()
                .map(Incentive::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.put("recentIncentives", recentTotal);
            summary.put("recentIncentiveCount", recentIncentives.size());

            // Group by client
            Map<String, BigDecimal> incentivesByClient = allIncentives.stream()
                .collect(Collectors.groupingBy(
                    incentive -> incentive.getClientEmployee().getClient().getName(),
                    Collectors.mapping(
                        Incentive::getTotalAmount,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
            summary.put("incentivesByClient", incentivesByClient);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/reports/monthly")
    public ResponseEntity<?> getMonthlyReport(@RequestParam Long employeeId,
                                            @RequestParam(required = false) String month) {
        try {
            LocalDate startDate, endDate;
            
            if (month != null) {
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

            List<Trip> monthlyTrips = tripRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
            List<Incentive> monthlyIncentives = incentiveRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
            
            Map<String, Object> monthlyReport = new HashMap<>();
            monthlyReport.put("period", startDate.toString() + " to " + endDate.toString());
            
            // Trip statistics
            monthlyReport.put("totalTrips", monthlyTrips.size());
            monthlyReport.put("totalDistance", monthlyTrips.stream()
                .mapToDouble(t -> t.getDistance().doubleValue()).sum());
            monthlyReport.put("totalDuration", monthlyTrips.stream()
                .mapToDouble(t -> t.getDuration().doubleValue()).sum());

            // Incentive statistics
            BigDecimal monthlyIncentiveTotal = monthlyIncentives.stream()
                .map(Incentive::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyReport.put("totalIncentives", monthlyIncentiveTotal);
            monthlyReport.put("incentiveCount", monthlyIncentives.size());

            
            Map<String, Object> dailyBreakdown = monthlyTrips.stream()
                .collect(Collectors.groupingBy(
                    trip -> trip.getTripDate().toString(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        trips -> {
                            Map<String, Object> dayStats = new HashMap<>();
                            dayStats.put("tripCount", trips.size());
                            dayStats.put("totalDistance", trips.stream()
                                .mapToDouble(t -> t.getDistance().doubleValue()).sum());
                            return dayStats;
                        }
                    )
                ));
            monthlyReport.put("dailyBreakdown", dailyBreakdown);

            return ResponseEntity.ok(monthlyReport);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}