package com.example.buspassmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.buspassmanagement.model.RouteInstallment;

@Repository
public interface RouteInstallmentRepository extends JpaRepository<RouteInstallment, Long> {
    
    /**
     * Find installment configuration by route name
     */
    Optional<RouteInstallment> findByRouteName(String routeName);
    
    /**
     * Check if installment configuration exists for a route
     */
    boolean existsByRouteName(String routeName);
}


