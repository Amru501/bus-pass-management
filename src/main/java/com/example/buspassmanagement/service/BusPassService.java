package com.example.buspassmanagement.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.buspassmanagement.model.BusPass;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.repository.BusPassRepository;

@Service
public class BusPassService {

    @Autowired
    private BusPassRepository busPassRepository;

    /**
     * Get or create bus pass for user
     */
    @Transactional
    public BusPass getOrCreateBusPass(User user) {
        return busPassRepository.findByUser(user)
            .orElseGet(() -> {
                BusPass newPass = new BusPass();
                newPass.setUser(user);
                newPass.setStatus(BusPass.PassStatus.INACTIVE);
                return busPassRepository.save(newPass);
            });
    }

    /**
     * Find bus pass by user
     */
    public Optional<BusPass> findByUser(User user) {
        return busPassRepository.findByUser(user);
    }

    /**
     * Find bus pass by user ID
     */
    public Optional<BusPass> findByUserId(Long userId) {
        return busPassRepository.findByUserId(userId);
    }

    /**
     * Save or update bus pass
     */
    @Transactional
    public BusPass save(BusPass busPass) {
        return busPassRepository.save(busPass);
    }

    /**
     * Set selected route for user's bus pass
     */
    @Transactional
    public void setSelectedRoute(User user, String routeName) {
        BusPass busPass = getOrCreateBusPass(user);
        busPass.setSelectedRoute(routeName);
        busPassRepository.save(busPass);
    }

    /**
     * Activate bus pass for user
     */
    @Transactional
    public void activatePass(User user) {
        BusPass busPass = getOrCreateBusPass(user);
        busPass.activate();
        busPassRepository.save(busPass);
    }

    /**
     * Deactivate bus pass for user
     */
    @Transactional
    public void deactivatePass(User user) {
        BusPass busPass = getOrCreateBusPass(user);
        busPass.deactivate();
        busPassRepository.save(busPass);
    }

    /**
     * Check if user's pass is active
     */
    public boolean isPassActive(User user) {
        return busPassRepository.findByUser(user)
            .map(BusPass::isActive)
            .orElse(false);
    }
}



