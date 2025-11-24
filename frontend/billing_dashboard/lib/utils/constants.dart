/// Application constants and configuration
class AppConstants {
  // API Configuration
  static const String apiBaseUrl = 'http://localhost:8082/api';
  static const Duration apiTimeout = Duration(seconds: 30);
  
  // App Information
  static const String appName = 'MoveInSync Billing Dashboard';
  static const String appVersion = '1.0.0';
  
  // Storage Keys
  static const String tokenKey = 'auth_token';
  static const String userKey = 'user_data';
  
  // Route Names
  static const String loginRoute = '/login';
  static const String adminDashboardRoute = '/admin';
  static const String vendorDashboardRoute = '/vendor';
  static const String clientDashboardRoute = '/client';
  static const String employeeDashboardRoute = '/employee';
  
  // User Roles
  static const String adminRole = 'ADMIN';
  static const String vendorRole = 'VENDOR';
  static const String employeeRole = 'EMPLOYEE';
  static const String clientRole = 'CLIENT';
  
  // UI Constants
  static const double defaultPadding = 16.0;
  static const double cardElevation = 4.0;
  static const double borderRadius = 8.0;
  
  // Colors
  static const int primaryColorValue = 0xFF2196F3;
  static const int accentColorValue = 0xFF03DAC6;
  
  // Chart Colors
  static const List<int> chartColors = [
    0xFF2196F3, // Blue
    0xFF4CAF50, // Green
    0xFFFF9800, // Orange
    0xFFE91E63, // Pink
    0xFF9C27B0, // Purple
    0xFF00BCD4, // Cyan
  ];
}

/// API Endpoints
class ApiEndpoints {
  // Authentication
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String profile = '/auth/profile';
  static const String refreshToken = '/auth/refresh-token';
  
  // Admin
  static const String adminAnalytics = '/admin/analytics';
  static const String adminClients = '/admin/clients';
  static const String adminVendors = '/admin/vendors';
  static const String adminEmployees = '/admin/employees';
  static const String adminAssignVendor = '/admin/assign-vendor';
  static const String adminAssignments = '/admin/assignments';
  
  // Vendor
  static const String vendorProfile = '/vendor/profile';
  static const String vendorClients = '/vendor/clients';
  static const String vendorTrips = '/vendor/trips';
  
  // Employee
  static const String employeeProfile = '/employee/profile';
  static const String employeeTrips = '/employee/trips';
  static const String employeeIncentives = '/employee/incentives';
  
  // Client
  static const String clientEmployees = '/client/employees';
  static const String clientTrips = '/client/trips';
}

/// Error Messages
class ErrorMessages {
  static const String networkError = 'Network error. Please check your connection.';
  static const String serverError = 'Server error. Please try again later.';
  static const String authenticationError = 'Authentication failed. Please login again.';
  static const String validationError = 'Please check your input and try again.';
  static const String unknownError = 'An unexpected error occurred.';
}

/// Success Messages
class SuccessMessages {
  static const String loginSuccess = 'Login successful!';
  static const String logoutSuccess = 'Logged out successfully!';
  static const String reportGenerated = 'Report generated successfully!';
  static const String dataUpdated = 'Data updated successfully!';
  static const String vendorAssigned = 'Vendor assigned successfully!';
}