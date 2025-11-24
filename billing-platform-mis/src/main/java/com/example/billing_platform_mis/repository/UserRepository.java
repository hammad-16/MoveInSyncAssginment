package com.example.billing_platform_mis.repository;

import com.example.billing_platform_mis.entity.User;
import com.example.billing_platform_mis.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Repository for User entity with authentication and role-based queries
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    //Find user by email for authentication
    Optional<User> findByEmail(String email);
    
    //Check if user exists by email (for registration validation)
    boolean existsByEmail(String email);
    
    //Find users by role for role-based operations
    List<User> findByRole(UserRole role);
    
    //Find users by role with pagination support
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.name ASC")
    List<User> findByRoleOrderByName(@Param("role") UserRole role);
    
    //Find all clients for admin management
    @Query("SELECT u FROM User u WHERE u.role = 'CLIENT' ORDER BY u.createdAt DESC")
    List<User> findAllClients();
    
    //Find all vendors for admin management
    @Query("SELECT u FROM User u WHERE u.role = 'VENDOR' ORDER BY u.createdAt DESC")
    List<User> findAllVendors();
    
    //Find all employees for client management
    @Query("SELECT u FROM User u WHERE u.role = 'EMPLOYEE' ORDER BY u.name ASC")
    List<User> findAllEmployees();
    
    //Find users by name pattern (for search functionality)
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) ORDER BY u.name ASC")
    List<User> findByNameContainingIgnoreCase(@Param("namePattern") String namePattern);
    
    //Find users by role and name pattern
    @Query("SELECT u FROM User u WHERE u.role = :role AND LOWER(u.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) ORDER BY u.name ASC")
    List<User> findByRoleAndNameContainingIgnoreCase(@Param("role") UserRole role, @Param("namePattern") String namePattern);
    
    //Count users by role for analytics
    long countByRole(UserRole role);
}