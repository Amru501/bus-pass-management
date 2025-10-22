package com.example.buspassmanagement.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.BusService;
import com.example.buspassmanagement.service.DriverService;
import com.example.buspassmanagement.service.UserService;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BusService busService;
    
    @Autowired
    private DriverService driverService;

    @GetMapping("/")
    public String homePage(Model model, Principal principal) {
        if (principal != null) {
            userService.findByEmail(principal.getName()).ifPresent(u -> model.addAttribute("user", u));
        }
        
        // Calculate statistics
        try {
            // Total buses count
            int totalBuses = busService.getAllBuses().size();
            model.addAttribute("totalBuses", totalBuses);
            
            // Total students count (users with ROLE_USER)
            long totalStudents = userService.getAllUsers().stream()
                .filter(user -> User.Role.USER.equals(user.getRole()))
                .count();
            model.addAttribute("totalStudents", totalStudents);
            
            // Total drivers count
            int totalDrivers = driverService.findAllDrivers().size();
            model.addAttribute("totalDrivers", totalDrivers);
            
        } catch (Exception e) {
            System.err.println("ERROR loading dashboard stats: " + e.getMessage());
            model.addAttribute("totalBuses", 0);
            model.addAttribute("totalStudents", 0);
            model.addAttribute("totalDrivers", 0);
        }
        
        return "home"; // this will look for home.html in src/main/resources/templates
    }
}
