package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.*;
import com.example.billing_platform_mis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@Transactional
public class TripService {
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private ClientVendorRepository clientVendorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClientEmployeeRepository clientEmployeeRepository;
    
    //Record a new trip with basic validation
    public Trip recordTrip(Long clientVendorId, Long employeeId, BigDecimal distance, 
                          BigDecimal duration, LocalDate tripDate) {
        
        //Basic validation
        if (duration == null || duration.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (tripDate == null || tripDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid trip date");
        }
        
        //Validate relationships exist
        ClientVendor clientVendor = clientVendorRepository.findById(clientVendorId)
            .orElseThrow(() -> new IllegalArgumentException("Client-vendor relationship not found"));
        
        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        //Verify employee belongs to client
        if (!clientEmployeeRepository.existsByClientIdAndEmployeeId(
                clientVendor.getClient().getId(), employeeId)) {
            throw new IllegalArgumentException("Employee does not belong to this client");
        }
        
        //Create and save trip
        Trip trip = new Trip();
        trip.setClientVendor(clientVendor);
        trip.setEmployee(employee);
        trip.setDistance(distance);
        trip.setDuration(duration);
        trip.setTripDate(tripDate);
        
        return tripRepository.save(trip);
    }
    
    //Get trips by user role with basic filtering
    public List<Trip> getTripsForUser(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        switch (user.getRole()) {
            case EMPLOYEE:
                return tripRepository.findByEmployeeIdAndDateRange(userId, startDate, endDate);
            case CLIENT:
                return tripRepository.findByClientIdAndDateRange(userId, startDate, endDate);
            case VENDOR:
                return tripRepository.findByVendorIdAndDateRange(userId, startDate, endDate);
            case ADMIN:
                return tripRepository.findAll().stream()
                    .filter(trip -> !trip.getTripDate().isBefore(startDate) && 
                                  !trip.getTripDate().isAfter(endDate))
                    .toList();
            default:
                throw new IllegalArgumentException("Invalid user role");
        }
    }
    
    //Get trips for specific client-vendor relationship
    public List<Trip> getTripsForClientVendor(Long clientVendorId, LocalDate startDate, LocalDate endDate) {
        return tripRepository.findByClientVendorIdAndDateRange(clientVendorId, startDate, endDate);
    }
    
    //Update trip (admin only for simplicity)
    public Trip updateTrip(Long tripId, BigDecimal distance, BigDecimal duration, LocalDate tripDate) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        
        //Basic validation
        if (distance.compareTo(BigDecimal.ZERO) <= 0 || duration.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Distance and duration must be positive");
        }
        
        trip.setDistance(distance);
        trip.setDuration(duration);
        trip.setTripDate(tripDate);
        
        return tripRepository.save(trip);
    }
}