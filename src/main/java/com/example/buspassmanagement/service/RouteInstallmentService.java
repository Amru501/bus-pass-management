package com.example.buspassmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.buspassmanagement.model.RouteInstallment;
import com.example.buspassmanagement.repository.RouteInstallmentRepository;

@Service
public class RouteInstallmentService {

    @Autowired
    private RouteInstallmentRepository routeInstallmentRepository;

    /**
     * Get all route installment configurations
     */
    public List<RouteInstallment> findAll() {
        return routeInstallmentRepository.findAll();
    }

    /**
     * Find route installment by ID
     */
    public Optional<RouteInstallment> findById(Long id) {
        return routeInstallmentRepository.findById(id);
    }

    /**
     * Find route installment by route name
     */
    public Optional<RouteInstallment> findByRouteName(String routeName) {
        return routeInstallmentRepository.findByRouteName(routeName);
    }

    /**
     * Save or update route installment configuration
     */
    @Transactional
    public RouteInstallment save(RouteInstallment routeInstallment) {
        // Calculate total fee before saving
        routeInstallment.calculateTotalFee();
        return routeInstallmentRepository.save(routeInstallment);
    }

    /**
     * Delete route installment configuration
     */
    @Transactional
    public void deleteById(Long id) {
        routeInstallmentRepository.deleteById(id);
    }

    /**
     * Check if installment configuration exists for a route
     */
    public boolean existsByRouteName(String routeName) {
        return routeInstallmentRepository.existsByRouteName(routeName);
    }
}


