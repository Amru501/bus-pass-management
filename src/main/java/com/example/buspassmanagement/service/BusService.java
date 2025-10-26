package com.example.buspassmanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.repository.BusRepository;
import com.example.buspassmanagement.repository.DriverRepository;

@Service
public class BusService {
    @Autowired
    private BusRepository busRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private com.example.buspassmanagement.repository.NoticeRepository noticeRepository;

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
    
    public boolean hasAssignedDrivers(Long busId) {
        Bus bus = getBusById(busId);
        if (bus == null) {
            return false;
        }
        return !driverRepository.findByAssignedBus(bus).isEmpty();
    }
    
    public boolean hasNotices(Long busId) {
        Bus bus = getBusById(busId);
        if (bus == null) {
            return false;
        }
        return !noticeRepository.findByBus(bus).isEmpty();
    }
    
    public void deleteNoticesForBus(Long busId) {
        Bus bus = getBusById(busId);
        if (bus != null) {
            noticeRepository.findByBus(bus).forEach(notice -> noticeRepository.delete(notice));
        }
    }
    
    public void deleteDriversForBus(Long busId) {
        Bus bus = getBusById(busId);
        if (bus != null) {
            driverRepository.findByAssignedBus(bus).forEach(driver -> driverRepository.delete(driver));
        }
    }
}
