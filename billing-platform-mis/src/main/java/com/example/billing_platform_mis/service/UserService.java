package com.example.billing_platform_mis.service;

import com.example.billing_platform_mis.entity.User;
import com.example.billing_platform_mis.entity.UserRole;
import com.example.billing_platform_mis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    //Register a new user
    public User registerUser(String name, String email, String password, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        
        return userRepository.save(user);
    }
    
    //Authenticate user and return JWT token
    public String authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        return jwtTokenService.generateToken(user);
    }
    
    //Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    //Find user by ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    //Get users by role
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
}