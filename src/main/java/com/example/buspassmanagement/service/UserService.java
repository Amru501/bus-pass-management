package com.example.buspassmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Assuming this is used

    // Existing methods:
    public User saveUser(User user) {
        // Hash the password before saving (crucial security step)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Ensure role is set, if not already handled by default value
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // 🔑 ADD THIS METHOD TO RESOLVE THE ERROR:
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // ... other methods like findById, delete, etc.
}