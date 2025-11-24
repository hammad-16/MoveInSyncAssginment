package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.User;
import com.example.billing_platform_mis.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//Simple authorization service for role-based access control
@Service
public class AuthorizationService {
    
    @Autowired
    private UserService userService;
    
    //Check if user has admin role
    public boolean isAdmin(Long userId) {
        return userService.findById(userId)
            .map(user -> user.getRole() == UserRole.ADMIN)
            .orElse(false);
    }
    
    //Check if user can access client data
    public boolean canAccessClientData(Long userId, Long clientId) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) return false;
        
        //Admin can access all client data
        if (user.getRole() == UserRole.ADMIN) return true;
        
        //Client can access their own data
        if (user.getRole() == UserRole.CLIENT && user.getId().equals(clientId)) return true;
        
        return false;
    }
    
    //Check if user can access vendor data
    public boolean canAccessVendorData(Long userId, Long vendorId) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) return false;
        
        //Admin can access all vendor data
        if (user.getRole() == UserRole.ADMIN) return true;
        
        //Vendor can access their own data
        if (user.getRole() == UserRole.VENDOR && user.getId().equals(vendorId)) return true;
        
        return false;
    }
    
    //Check if user can access employee data
    public boolean canAccessEmployeeData(Long userId, Long employeeId) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) return false;
        
        //Admin can access all employee data
        if (user.getRole() == UserRole.ADMIN) return true;
        
        //Employee can access their own data
        if (user.getRole() == UserRole.EMPLOYEE && user.getId().equals(employeeId)) return true;
        
        return false;
    }
}