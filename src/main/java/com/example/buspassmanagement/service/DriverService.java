package com.example.buspassmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.buspassmanagement.model.Driver;
import com.example.buspassmanagement.repository.DriverRepository;

@Service
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;

    public List<Driver> findAllDrivers() {
        return driverRepository.findAll();
    }

    public void saveDriver(Driver driver) {
        driverRepository.save(driver);
    }

    public Optional<Driver> findByEmail(String email) {
        return driverRepository.findByEmail(email);
    }
}

