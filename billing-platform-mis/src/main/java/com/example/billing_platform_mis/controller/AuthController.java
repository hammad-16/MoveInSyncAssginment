package com.example.billing_platform_mis.controller;

import com.example.billing_platform_mis.entity.User;
import com.example.billing_platform_mis.entity.UserRole;
import com.example.billing_platform_mis.entity.VendorProfile;
import com.example.billing_platform_mis.entity.ClientProfile;
import com.example.billing_platform_mis.entity.BillingModel;
import com.example.billing_platform_mis.service.UserService;
import com.example.billing_platform_mis.service.JwtTokenService;
import com.example.billing_platform_mis.repository.VendorProfileRepository;
import com.example.billing_platform_mis.repository.ClientProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private VendorProfileRepository vendorProfileRepository;
    
    @Autowired
    private ClientProfileRepository clientProfileRepository;
    
    //User login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user credentials using UserService
            String token = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            

            User user = userService.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
            

            LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString(),
                "Login successful"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {

            ErrorResponse error = new ErrorResponse("AUTHENTICATION_FAILED", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {

            UserRole role;
            try {
                role = UserRole.valueOf(registerRequest.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                ErrorResponse error = new ErrorResponse("INVALID_ROLE", "Invalid user role provided");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Register new user using UserService
            User newUser = userService.registerUser(
                registerRequest.getName(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                role
            );
            

            if (registerRequest.getPreferredBillingModel() != null 
                && !registerRequest.getPreferredBillingModel().trim().isEmpty()) {
                try {
                    BillingModel billingModel = BillingModel.valueOf(registerRequest.getPreferredBillingModel().toUpperCase().trim());
                    
                    if (role == UserRole.VENDOR) {

                        VendorProfile vendorProfile = new VendorProfile();
                        vendorProfile.setUser(newUser);
                        vendorProfile.setPreferredBillingModel(billingModel);
                        vendorProfile.setBillingModel(billingModel); // Set both fields for consistency
                        vendorProfile.setMaxClientCapacity(10); // Default capacity
                        vendorProfile.setAvailableVehicles(1); // Default vehicles
                        vendorProfile.setServiceQualityRating(java.math.BigDecimal.valueOf(5.0)); // Default rating
                        vendorProfile.setGeographicCoverage("City-wide"); // Default coverage
                        
                        // Set default rates based on billing model
                        switch (billingModel) {
                            case PACKAGE:
                                vendorProfile.setDefaultPackageRate(java.math.BigDecimal.valueOf(1000.0)); // $1000/month
                                break;
                            case TRIP:
                                vendorProfile.setDefaultTripRate(java.math.BigDecimal.valueOf(50.0)); // $50/trip
                                break;
                            case HYBRID:
                                vendorProfile.setDefaultPackageRate(java.math.BigDecimal.valueOf(500.0)); // $500/month base
                                vendorProfile.setDefaultTripRate(java.math.BigDecimal.valueOf(25.0)); // $25/trip additional
                                break;
                        }
                        
                        // Set default overage rates for all billing models
                        vendorProfile.setDefaultExtraDistanceRate(java.math.BigDecimal.valueOf(3.0)); // $3/km
                        vendorProfile.setDefaultExtraTimeRate(java.math.BigDecimal.valueOf(2.5)); // $2.5/min
                        
                        vendorProfileRepository.save(vendorProfile);
                    } else if (role == UserRole.CLIENT) {

                        ClientProfile clientProfile = new ClientProfile();
                        clientProfile.setUser(newUser);
                        clientProfile.setPreferredBillingModel(billingModel);
                        clientProfile.setCompanyName(newUser.getName()); 
                        clientProfile.setBusinessType("General");
                        clientProfile.setExpectedMonthlyTrips(50);
                        clientProfile.setBudgetRangeMin(java.math.BigDecimal.valueOf(500));
                        clientProfile.setBudgetRangeMax(java.math.BigDecimal.valueOf(2000));
                        clientProfile.setServiceArea("City-wide");
                        
                        clientProfileRepository.save(clientProfile);
                    }
                } catch (IllegalArgumentException e) {

                    System.out.println("Invalid billing model provided during registration: " + registerRequest.getPreferredBillingModel());
                }
            }
            
            // Generate JWT token for immediate login
            String token = jwtTokenService.generateToken(newUser);
            

            RegisterResponse response = new RegisterResponse(
                newUser.getId(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getRole().toString(),
                token,
                "Registration successful"
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {

            ErrorResponse error = new ErrorResponse("REGISTRATION_FAILED", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Registration failed due to server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Token refresh endpoint
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            // Validate the existing token
            if (!jwtTokenService.validateToken(refreshRequest.getToken())) {
                ErrorResponse error = new ErrorResponse("INVALID_TOKEN", "Token is invalid or expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Extract user information from token
            String email = jwtTokenService.getEmailFromToken(refreshRequest.getToken());
            User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate new token
            String newToken = jwtTokenService.generateToken(user);
            
            // Return new token
            RefreshTokenResponse response = new RefreshTokenResponse(newToken, "Token refreshed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {

            ErrorResponse error = new ErrorResponse("TOKEN_REFRESH_FAILED", "Unable to refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from Authorization header
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                ErrorResponse error = new ErrorResponse("MISSING_TOKEN", "Authorization token is required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate token
            if (!jwtTokenService.validateToken(token)) {
                ErrorResponse error = new ErrorResponse("INVALID_TOKEN", "Token is invalid or expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Get user from token
            Long userId = jwtTokenService.getUserIdFromToken(token);
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Return user profile (without sensitive data)
            UserProfileResponse profile = new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {

            ErrorResponse error = new ErrorResponse("PROFILE_ERROR", "Unable to retrieve user profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                         @Valid @RequestBody UpdateProfileRequest updateRequest) {
        try {
            // Extract and validate token
            String token = extractTokenFromHeader(authHeader);
            if (token == null || !jwtTokenService.validateToken(token)) {
                ErrorResponse error = new ErrorResponse("UNAUTHORIZED", "Valid authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Get current user
            Long userId = jwtTokenService.getUserIdFromToken(token);
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update user information 
            user.setName(updateRequest.getName());
      
            //  Save updated user (this would typically go through UserService)
            UserProfileResponse updatedProfile = new UserProfileResponse(
                user.getId(),
                updateRequest.getName(),
                user.getEmail(),
                user.getRole().toString(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            
            return ResponseEntity.ok(updatedProfile);
            
        } catch (Exception e) {

            ErrorResponse error = new ErrorResponse("UPDATE_FAILED", "Unable to update profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    //Password reset request 
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        try {
            // Check if user exists
            User user = userService.findByEmail(resetRequest.getEmail())
                .orElse(null);
            
          
            PasswordResetResponse response = new PasswordResetResponse(
                "If the email exists in our system, a password reset link has been sent"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {

            ErrorResponse error = new ErrorResponse("RESET_ERROR", "Unable to process password reset");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    //Helper method to extract JWT token from Authorization header
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); 
        }
        return null;
    }
    

    

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        
        
        public LoginRequest() {}
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
        
       
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    

    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        
        @NotBlank(message = "Role is required")
        private String role;
        
        private String preferredBillingModel; 
        
        
        public RegisterRequest() {}
        public RegisterRequest(String name, String email, String password, String role) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
        }
        
        public RegisterRequest(String name, String email, String password, String role, String preferredBillingModel) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.preferredBillingModel = preferredBillingModel;
        }
        
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getPreferredBillingModel() { return preferredBillingModel; }
        public void setPreferredBillingModel(String preferredBillingModel) { this.preferredBillingModel = preferredBillingModel; }
    }
    
    public static class RefreshTokenRequest {
        @NotBlank(message = "Token is required")
        private String token;
        
        public RefreshTokenRequest() {}
        public RefreshTokenRequest(String token) { this.token = token; }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    
    public static class UpdateProfileRequest {
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;
        
        public UpdateProfileRequest() {}
        public UpdateProfileRequest(String name) { this.name = name; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class PasswordResetRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        public PasswordResetRequest() {}
        public PasswordResetRequest(String email) { this.email = email; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class LoginResponse {
        private String token;
        private Long userId;
        private String name;
        private String email;
        private String role;
        private String message;
        
        public LoginResponse(String token, Long userId, String name, String email, String role, String message) {
            this.token = token;
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.message = message;
        }
        
        
        public String getToken() { return token; }
        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getMessage() { return message; }
    }
    

    public static class RegisterResponse {
        private Long userId;
        private String name;
        private String email;
        private String role;
        private String token;
        private String message;
        
        public RegisterResponse(Long userId, String name, String email, String role, String token, String message) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.token = token;
            this.message = message;
        }
        
        
        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getToken() { return token; }
        public String getMessage() { return message; }
    }
    

    public static class RefreshTokenResponse {
        private String token;
        private String message;
        
        public RefreshTokenResponse(String token, String message) {
            this.token = token;
            this.message = message;
        }
        
        public String getToken() { return token; }
        public String getMessage() { return message; }
    }
    

    public static class UserProfileResponse {
        private Long userId;
        private String name;
        private String email;
        private String role;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        
        public UserProfileResponse(Long userId, String name, String email, String role, 
                                 java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        
        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    }
    

    public static class PasswordResetResponse {
        private String message;
        
        public PasswordResetResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
    

    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private long timestamp;
        
        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}