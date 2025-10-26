package com.example.buspassmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.model.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    // This new method allows the service to find a driver by their email address.
    Optional<Driver> findByEmail(String email);
    
    // Find all drivers assigned to a specific bus
    List<Driver> findByAssignedBus(Bus bus);
}

