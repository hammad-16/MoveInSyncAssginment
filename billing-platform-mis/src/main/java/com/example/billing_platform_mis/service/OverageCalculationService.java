package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.*;
import com.example.billing_platform_mis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@Transactional
public class OverageCalculationService {
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private ClientVendorRepository clientVendorRepository;
    
    @Autowired
    private IncentiveRepository incentiveRepository;
    
    @Autowired
    private PayoutRepository payoutRepository;
    
    @Autowired
    private ClientEmployeeRepository clientEmployeeRepository;
    
    //Process trip for overages and create incentives/payouts
    public void processTrip(Trip trip) {
        ClientVendor relationship = trip.getClientVendor();
        
        //Calculate distance overage
        BigDecimal distanceOverage = calculateDistanceOverage(trip, relationship);
        
        //Calculate time overage
        BigDecimal timeOverage = calculateTimeOverage(trip, relationship);
        
        //Create employee incentive if there are overages
        if (distanceOverage.compareTo(BigDecimal.ZERO) > 0 || timeOverage.compareTo(BigDecimal.ZERO) > 0) {
            createEmployeeIncentive(trip, distanceOverage, timeOverage, relationship);
        }
        
        //Create vendor payout
        createVendorPayout(trip, distanceOverage, timeOverage, relationship);
    }
    
    //Calculate distance overage amount
    private BigDecimal calculateDistanceOverage(Trip trip, ClientVendor relationship) {
        BigDecimal standardLimit = relationship.getStandardDistanceLimit();
        if (standardLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        
        BigDecimal overage = trip.getDistance().subtract(standardLimit);
        return overage.max(BigDecimal.ZERO);
    }
    
    //Calculate time overage amount
    private BigDecimal calculateTimeOverage(Trip trip, ClientVendor relationship) {
        BigDecimal standardLimit = relationship.getStandardTimeLimit();
        if (standardLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        
        BigDecimal overage = trip.getDuration().subtract(standardLimit);
        return overage.max(BigDecimal.ZERO);
    }
    
    //Create employee incentive for overages
    private void createEmployeeIncentive(Trip trip, BigDecimal distanceOverage, BigDecimal timeOverage, ClientVendor relationship) {
        //Find client-employee relationship
        ClientEmployee clientEmployee = clientEmployeeRepository
            .findByClientIdAndEmployeeId(relationship.getClient().getId(), trip.getEmployee().getId())
            .orElse(null);
        
        if (clientEmployee == null) return;
        
        //Calculate incentive amounts
        BigDecimal distanceIncentive = distanceOverage.multiply(relationship.getEmployeeExtraDistanceRate());
        BigDecimal timeIncentive = timeOverage.multiply(relationship.getEmployeeExtraTimeRate());
        BigDecimal totalIncentive = distanceIncentive.add(timeIncentive);
        
        //Create incentive record
        Incentive incentive = new Incentive();
        incentive.setClientEmployee(clientEmployee);
        incentive.setTrip(trip);
        incentive.setDistanceIncentive(distanceIncentive);
        incentive.setTimeIncentive(timeIncentive);
        incentive.setTotalAmount(totalIncentive);
        
        incentiveRepository.save(incentive);
    }
    
    //Create vendor payout for trip
    private void createVendorPayout(Trip trip, BigDecimal distanceOverage, BigDecimal timeOverage, ClientVendor relationship) {
        //Calculate base payout (could be trip rate or package allocation)
        BigDecimal baseAmount = relationship.getTripRate();
        
        //Calculate overage payouts
        BigDecimal distanceOveragePayout = distanceOverage.multiply(relationship.getVendorExtraDistanceRate());
        BigDecimal timeOveragePayout = timeOverage.multiply(relationship.getVendorExtraTimeRate());
        BigDecimal totalAmount = baseAmount.add(distanceOveragePayout).add(timeOveragePayout);
        
        //Create payout record
        Payout payout = new Payout();
        payout.setClientVendor(relationship);
        payout.setTrip(trip);
        payout.setBaseAmount(baseAmount);
        payout.setDistanceOverage(distanceOveragePayout);
        payout.setTimeOverage(timeOveragePayout);
        payout.setTotalAmount(totalAmount);
        
        payoutRepository.save(payout);
    }
    
    //Process all unprocessed trips for overages
    public void processUnprocessedTrips() {
        List<Trip> tripsExceedingLimits = tripRepository.findTripsExceedingDistanceLimit();
        tripsExceedingLimits.addAll(tripRepository.findTripsExceedingTimeLimit());
        
        for (Trip trip : tripsExceedingLimits) {
            //Check if already processed
            if (!incentiveRepository.existsByTripId(trip.getId()) && 
                !payoutRepository.existsByTripId(trip.getId())) {
                processTrip(trip);
            }
        }
    }
}