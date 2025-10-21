package com.example.buspassmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.buspassmanagement.model.BusPass;
import com.example.buspassmanagement.model.User;

@Repository
public interface BusPassRepository extends JpaRepository<BusPass, Long> {
    
    /**
     * Find bus pass by user
     */
    Optional<BusPass> findByUser(User user);
    
    /**
     * Find bus pass by user ID
     */
    Optional<BusPass> findByUserId(Long userId);
    
    /**
     * Check if user has a bus pass
     */
    boolean existsByUser(User user);
    
    /**
     * Delete bus pass by user
     */
    void deleteByUser(User user);
}


