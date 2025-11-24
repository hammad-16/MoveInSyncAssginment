package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.User;
import com.example.billing_platform_mis.entity.UserRole;
import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.entity.BillingModel;
import com.example.billing_platform_mis.repository.UserRepository;
import com.example.billing_platform_mis.repository.VendorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VendorProfileRepository vendorProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    //This is for the first time run of the application
    @Override
    public void run(String... args) throws Exception {
        
        if (userRepository.count() == 0) {
            
            // Create Admin user
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@company.com");
            admin.setPasswordHash(passwordEncoder.encode("password123"));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
            
            // Create multiple Vendor users with different billing models
            User packageVendor = new User();
            packageVendor.setName("Package Transport Co");
            packageVendor.setEmail("package@transport.com");
            packageVendor.setPasswordHash(passwordEncoder.encode("password123"));
            packageVendor.setRole(UserRole.VENDOR);
            userRepository.save(packageVendor);
            
            // Create Package vendor profile
            VendorProfile packageProfile = new VendorProfile();
            packageProfile.setUser(packageVendor);
            packageProfile.setBillingModel(BillingModel.PACKAGE);
            packageProfile.setPreferredBillingModel(BillingModel.PACKAGE);
            packageProfile.setMaxClientCapacity(10);
            packageProfile.setAvailableVehicles(5);
            packageProfile.setServiceQualityRating(java.math.BigDecimal.valueOf(4.5));
            packageProfile.setGeographicCoverage("City-wide");
            packageProfile.setDefaultPackageRate(java.math.BigDecimal.valueOf(1000.0));
            packageProfile.setDefaultExtraDistanceRate(java.math.BigDecimal.valueOf(3.0));
            packageProfile.setDefaultExtraTimeRate(java.math.BigDecimal.valueOf(2.5));
            vendorProfileRepository.save(packageProfile);
            
            User tripVendor = new User();
            tripVendor.setName("Trip-by-Trip Services");
            tripVendor.setEmail("trip@services.com");
            tripVendor.setPasswordHash(passwordEncoder.encode("password123"));
            tripVendor.setRole(UserRole.VENDOR);
            userRepository.save(tripVendor);
            
            // Create Trip vendor profile
            VendorProfile tripProfile = new VendorProfile();
            tripProfile.setUser(tripVendor);
            tripProfile.setBillingModel(BillingModel.TRIP);
            tripProfile.setPreferredBillingModel(BillingModel.TRIP);
            tripProfile.setMaxClientCapacity(15);
            tripProfile.setAvailableVehicles(8);
            tripProfile.setServiceQualityRating(java.math.BigDecimal.valueOf(4.2));
            tripProfile.setGeographicCoverage("Metropolitan area");
            tripProfile.setDefaultTripRate(java.math.BigDecimal.valueOf(50.0));
            tripProfile.setDefaultExtraDistanceRate(java.math.BigDecimal.valueOf(3.5));
            tripProfile.setDefaultExtraTimeRate(java.math.BigDecimal.valueOf(3.0));
            vendorProfileRepository.save(tripProfile);
            
            User hybridVendor = new User();
            hybridVendor.setName("Flexible Transport Solutions");
            hybridVendor.setEmail("hybrid@flexible.com");
            hybridVendor.setPasswordHash(passwordEncoder.encode("password123"));
            hybridVendor.setRole(UserRole.VENDOR);
            userRepository.save(hybridVendor);
            
            // Create Hybrid vendor profile
            VendorProfile hybridProfile = new VendorProfile();
            hybridProfile.setUser(hybridVendor);
            hybridProfile.setBillingModel(BillingModel.HYBRID);
            hybridProfile.setPreferredBillingModel(BillingModel.HYBRID);
            hybridProfile.setMaxClientCapacity(20);
            hybridProfile.setAvailableVehicles(12);
            hybridProfile.setServiceQualityRating(java.math.BigDecimal.valueOf(4.8));
            hybridProfile.setGeographicCoverage("Regional coverage");
            hybridProfile.setDefaultPackageRate(java.math.BigDecimal.valueOf(500.0));
            hybridProfile.setDefaultTripRate(java.math.BigDecimal.valueOf(25.0));
            hybridProfile.setDefaultExtraDistanceRate(java.math.BigDecimal.valueOf(2.5));
            hybridProfile.setDefaultExtraTimeRate(java.math.BigDecimal.valueOf(2.0));
            vendorProfileRepository.save(hybridProfile);
            
            // Create Employee user
            User employee = new User();
            employee.setName("Employee User");
            employee.setEmail("employee@company.com");
            employee.setPasswordHash(passwordEncoder.encode("password123"));
            employee.setRole(UserRole.EMPLOYEE);
            userRepository.save(employee);
            
            // Create Client user
            User client = new User();
            client.setName("Client User");
            client.setEmail("client@company.com");
            client.setPasswordHash(passwordEncoder.encode("password123"));
            client.setRole(UserRole.CLIENT);
            userRepository.save(client);
            

        }
    }
}