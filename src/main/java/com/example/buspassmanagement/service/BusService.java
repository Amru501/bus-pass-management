package com.example.buspassmanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.repository.BusRepository;

@Service
public class BusService {
    @Autowired
    private BusRepository busRepository;

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public void saveBus(Bus bus) {
        busRepository.save(bus);
    }

    public void deleteBus(Long id) {
        busRepository.deleteById(id);
    }
    
    public Bus getBusById(Long id) {
        return busRepository.findById(id).orElse(null); 
    }
}
