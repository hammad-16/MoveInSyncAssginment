package com.example.billing_platform_mis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.billing_platform_mis.entity.User;
import com.example.billing_platform_mis.entity.UserRole;
import com.example.billing_platform_mis.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*") // Allow all origins for development
public class PublicController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private UserRepository userRepository;
    
    //Basic health check endpoint
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // Step 1: Create basic health response
            HealthResponse health = new HealthResponse(
                "UP",
                "Billing Platform API is running",
                LocalDateTime.now(),
                "1.0.0"
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            // Step 2: Handle any errors
            HealthResponse health = new HealthResponse(
                "DOWN",
                "Service is experiencing issues: " + e.getMessage(),
                LocalDateTime.now(),
                "1.0.0"
            );
            
            return ResponseEntity.status(503).body(health);
        }
    }
    
    //Detailed system status endpoint
    @GetMapping("/status")
    public ResponseEntity<?> systemStatus() {
        try {
            // Step 1: Check database connectivity
            boolean databaseUp = checkDatabaseConnection();
            
            // Step 2: Get system information
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            // Step 3: Create detailed status response
            SystemStatusResponse status = new SystemStatusResponse(
                databaseUp ? "UP" : "DOWN",
                LocalDateTime.now(),
                "1.0.0",
                databaseUp,
                formatBytes(totalMemory),
                formatBytes(usedMemory),
                formatBytes(freeMemory),
                Runtime.getRuntime().availableProcessors(),
                System.getProperty("java.version"),
                System.getProperty("os.name")
            );
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            // Step 4: Handle errors
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("status", "ERROR");
            errorStatus.put("message", "Unable to retrieve system status");
            errorStatus.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorStatus);
        }
    }
    
    //Helper method to check database connection
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (Exception e) {
            return false;
        }
    }
    
    // Get available clients for employee registration
    @GetMapping("/clients")
    public ResponseEntity<?> getAvailableClients() {
        try {
            List<User> clients = userRepository.findByRole(UserRole.CLIENT);
            
            // Return only basic information needed for registration
            List<Map<String, Object>> clientList = clients.stream()
                .map(client -> {
                    Map<String, Object> clientInfo = new HashMap<>();
                    clientInfo.put("id", client.getId());
                    clientInfo.put("name", client.getName());
                    clientInfo.put("email", client.getEmail());
                    return clientInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(clientList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    //Helper method to format bytes in human readable format
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public static class HealthResponse {
        private String status;
        private String message;
        private LocalDateTime timestamp;
        private String version;
        
        public HealthResponse(String status, String message, LocalDateTime timestamp, String version) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            this.version = version;
        }
        
        // Getters
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getVersion() { return version; }
    }
    
    //Detailed system status response
    public static class SystemStatusResponse {
        private String status;
        private LocalDateTime timestamp;
        private String version;
        private boolean databaseConnected;
        private String totalMemory;
        private String usedMemory;
        private String freeMemory;
        private int availableProcessors;
        private String javaVersion;
        private String operatingSystem;
        
        public SystemStatusResponse(String status, LocalDateTime timestamp, String version,
                                  boolean databaseConnected, String totalMemory, String usedMemory,
                                  String freeMemory, int availableProcessors, String javaVersion,
                                  String operatingSystem) {
            this.status = status;
            this.timestamp = timestamp;
            this.version = version;
            this.databaseConnected = databaseConnected;
            this.totalMemory = totalMemory;
            this.usedMemory = usedMemory;
            this.freeMemory = freeMemory;
            this.availableProcessors = availableProcessors;
            this.javaVersion = javaVersion;
            this.operatingSystem = operatingSystem;
        }
        
        // Getters
        public String getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getVersion() { return version; }
        public boolean isDatabaseConnected() { return databaseConnected; }
        public String getTotalMemory() { return totalMemory; }
        public String getUsedMemory() { return usedMemory; }
        public String getFreeMemory() { return freeMemory; }
        public int getAvailableProcessors() { return availableProcessors; }
        public String getJavaVersion() { return javaVersion; }
        public String getOperatingSystem() { return operatingSystem; }
    }
}