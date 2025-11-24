package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.*;
import com.example.billing_platform_mis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
public class ReportService {
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private IncentiveRepository incentiveRepository;
    
    @Autowired
    private PayoutRepository payoutRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private BillingEngineService billingEngineService;
    
    //Generate employee report - trips and incentives (tenant isolated)
    public EmployeeReport generateEmployeeReport(Long employeeId, Long requestingUserId, 
                                               LocalDate startDate, LocalDate endDate) {
        

        User requestingUser = userRepository.findById(requestingUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (requestingUser.getRole() == UserRole.EMPLOYEE && !requestingUserId.equals(employeeId)) {
            throw new SecurityException("Employees can only view their own reports");
        }
        
        //Get employee trips (tenant isolated by repository)
        List<Trip> trips = tripRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        
        //Get employee incentives (tenant isolated by repository)
        List<Incentive> incentives = incentiveRepository.findByEmployeeIdWithTripDetailsAndDateRange(
            employeeId, startDate, endDate);
        
        //Calculate totals
        BigDecimal totalIncentives = incentiveRepository.calculateTotalIncentiveByEmployeeAndDateRange(
            employeeId, startDate, endDate);
        
        //Create audit record
        createReportAudit(requestingUserId, "EMPLOYEE_REPORT", employeeId);
        
        return new EmployeeReport(employeeId, trips, incentives, totalIncentives, startDate, endDate);
    }
    
    //Generate vendor report - billing and payouts (tenant isolated)
    public VendorReport generateVendorReport(Long vendorId, Long requestingUserId, 
                                           LocalDate startDate, LocalDate endDate) {
        

        User requestingUser = userRepository.findById(requestingUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (requestingUser.getRole() == UserRole.VENDOR && !requestingUserId.equals(vendorId)) {
            throw new SecurityException("Vendors can only view their own reports");
        }
        
        //Get vendor trips 
        List<Trip> trips = tripRepository.findByVendorIdWithClientDetailsAndDateRange(
            vendorId, startDate, endDate);
        
        //Get vendor payouts
        List<Payout> payouts = payoutRepository.findByVendorIdWithDetailsAndDateRange(
            vendorId, startDate, endDate);
        
        //Calculate totals
        BigDecimal totalPayouts = payoutRepository.calculateTotalPayoutByVendorAndDateRange(
            vendorId, startDate, endDate);
        
        //Create audit record
        createReportAudit(requestingUserId, "VENDOR_REPORT", vendorId);
        
        return new VendorReport(vendorId, trips, payouts, totalPayouts, startDate, endDate);
    }
    

    public ClientReport generateClientReport(Long clientId, Long requestingUserId, 
                                           LocalDate startDate, LocalDate endDate) {
        
        //Clients can only see their own reports
        User requestingUser = userRepository.findById(requestingUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (requestingUser.getRole() == UserRole.CLIENT && !requestingUserId.equals(clientId)) {
            throw new SecurityException("Clients can only view their own reports");
        }
        
        //Get client trips (tenant isolated by repository)
        List<Trip> trips = tripRepository.findByClientIdWithEmployeeDetailsAndDateRange(
            clientId, startDate, endDate);
        
        //Get client incentives (tenant isolated by repository)
        List<Incentive> incentives = incentiveRepository.findByClientIdWithDetailsAndDateRange(
            clientId, startDate, endDate);
        
        //Get client payouts (tenant isolated by repository)
        List<Payout> payouts = payoutRepository.findByClientIdWithDetailsAndDateRange(
            clientId, startDate, endDate);
        
        //Calculate totals
        BigDecimal totalIncentives = incentiveRepository.calculateTotalIncentiveByClientAndDateRange(
            clientId, startDate, endDate);
        BigDecimal totalPayouts = payoutRepository.calculateTotalPayoutByClientAndDateRange(
            clientId, startDate, endDate);
        
        //Create audit record
        createReportAudit(requestingUserId, "CLIENT_REPORT", clientId);
        
        return new ClientReport(clientId, trips, incentives, payouts, 
                              totalIncentives, totalPayouts, startDate, endDate);
    }
    
    //Generate admin cross-tenant report 
    public AdminReport generateAdminReport(Long requestingUserId, LocalDate startDate, LocalDate endDate) {
        
        //Security check: admin only
        User requestingUser = userRepository.findById(requestingUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (requestingUser.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Only admins can view cross-tenant reports");
        }
        
        //Get system-wide data (no tenant filtering for admin)
        long totalTrips = tripRepository.countTripsByDateRange(startDate, endDate);
        BigDecimal totalIncentives = incentiveRepository.calculateTotalIncentiveAmount();
        BigDecimal totalPayouts = payoutRepository.calculateTotalPayoutAmount();
        
        //Get top performers
        List<Object[]> topEmployees = incentiveRepository.findTopEmployeesByIncentiveAmount(startDate, endDate);
        List<Object[]> topVendors = payoutRepository.findTopVendorsByPayoutAmount(startDate, endDate);
        
        //Create audit record
        createReportAudit(requestingUserId, "ADMIN_REPORT", null);
        
        return new AdminReport(totalTrips, totalIncentives, totalPayouts, 
                             topEmployees, topVendors, startDate, endDate);
    }
    
    //Get user's report history (tenant isolated)
    public List<Report> getUserReportHistory(Long userId) {
        return reportRepository.findByGeneratedByIdOrderByCreatedAtDesc(userId);
    }
    
    //Audit record for report generation
    private void createReportAudit(Long userId, String reportType, Long targetEntityId) {
        Report auditRecord = new Report();
        auditRecord.setGeneratedBy(userRepository.findById(userId).orElse(null));
        auditRecord.setReportType(reportType);
        auditRecord.setTargetEntityId(targetEntityId);
        auditRecord.setDateFrom(LocalDate.now());
        auditRecord.setDateTo(LocalDate.now());
        
        reportRepository.save(auditRecord);
    }
    
    
    public static class EmployeeReport {
        private final Long employeeId;
        private final List<Trip> trips;
        private final List<Incentive> incentives;
        private final BigDecimal totalIncentives;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public EmployeeReport(Long employeeId, List<Trip> trips, List<Incentive> incentives,
                            BigDecimal totalIncentives, LocalDate startDate, LocalDate endDate) {
            this.employeeId = employeeId;
            this.trips = trips;
            this.incentives = incentives;
            this.totalIncentives = totalIncentives;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters
        public Long getEmployeeId() { return employeeId; }
        public List<Trip> getTrips() { return trips; }
        public List<Incentive> getIncentives() { return incentives; }
        public BigDecimal getTotalIncentives() { return totalIncentives; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getTripCount() { return trips.size(); }
        public int getIncentiveCount() { return incentives.size(); }
    }
    
    public static class VendorReport {
        private final Long vendorId;
        private final List<Trip> trips;
        private final List<Payout> payouts;
        private final BigDecimal totalPayouts;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public VendorReport(Long vendorId, List<Trip> trips, List<Payout> payouts,
                          BigDecimal totalPayouts, LocalDate startDate, LocalDate endDate) {
            this.vendorId = vendorId;
            this.trips = trips;
            this.payouts = payouts;
            this.totalPayouts = totalPayouts;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters
        public Long getVendorId() { return vendorId; }
        public List<Trip> getTrips() { return trips; }
        public List<Payout> getPayouts() { return payouts; }
        public BigDecimal getTotalPayouts() { return totalPayouts; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getTripCount() { return trips.size(); }
        public int getPayoutCount() { return payouts.size(); }
    }
    
    public static class ClientReport {
        private final Long clientId;
        private final List<Trip> trips;
        private final List<Incentive> incentives;
        private final List<Payout> payouts;
        private final BigDecimal totalIncentives;
        private final BigDecimal totalPayouts;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public ClientReport(Long clientId, List<Trip> trips, List<Incentive> incentives,
                          List<Payout> payouts, BigDecimal totalIncentives, BigDecimal totalPayouts,
                          LocalDate startDate, LocalDate endDate) {
            this.clientId = clientId;
            this.trips = trips;
            this.incentives = incentives;
            this.payouts = payouts;
            this.totalIncentives = totalIncentives;
            this.totalPayouts = totalPayouts;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters
        public Long getClientId() { return clientId; }
        public List<Trip> getTrips() { return trips; }
        public List<Incentive> getIncentives() { return incentives; }
        public List<Payout> getPayouts() { return payouts; }
        public BigDecimal getTotalIncentives() { return totalIncentives; }
        public BigDecimal getTotalPayouts() { return totalPayouts; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getTripCount() { return trips.size(); }
        public int getIncentiveCount() { return incentives.size(); }
        public int getPayoutCount() { return payouts.size(); }
    }
    
    public static class AdminReport {
        private final long totalTrips;
        private final BigDecimal totalIncentives;
        private final BigDecimal totalPayouts;
        private final List<Object[]> topEmployees;
        private final List<Object[]> topVendors;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public AdminReport(long totalTrips, BigDecimal totalIncentives, BigDecimal totalPayouts,
                         List<Object[]> topEmployees, List<Object[]> topVendors,
                         LocalDate startDate, LocalDate endDate) {
            this.totalTrips = totalTrips;
            this.totalIncentives = totalIncentives;
            this.totalPayouts = totalPayouts;
            this.topEmployees = topEmployees;
            this.topVendors = topVendors;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters
        public long getTotalTrips() { return totalTrips; }
        public BigDecimal getTotalIncentives() { return totalIncentives; }
        public BigDecimal getTotalPayouts() { return totalPayouts; }
        public List<Object[]> getTopEmployees() { return topEmployees; }
        public List<Object[]> getTopVendors() { return topVendors; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }
}