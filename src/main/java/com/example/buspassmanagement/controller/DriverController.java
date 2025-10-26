package com.example.buspassmanagement.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.model.Driver;
import com.example.buspassmanagement.service.BusService;
import com.example.buspassmanagement.service.DriverService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/drivers")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @Autowired
    private BusService busService;

    @GetMapping
    public String listDrivers(Model model) {
        model.addAttribute("drivers", driverService.findAllDrivers());
        model.addAttribute("buses", busService.getAllBuses());
        
        if (!model.containsAttribute("newDriverProfile")) {
            model.addAttribute("newDriverProfile", new Driver());
        }
        return "drivers";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String addDriver(@Valid @ModelAttribute("newDriverProfile") Driver driver,
                            BindingResult result,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            // *** THE FIX: Part 1 ***
                            // Explicitly capture the assignedBusId from the form.
                            @RequestParam(value = "assignedBusId", required = false) Long assignedBusId,
                            RedirectAttributes redirectAttributes) {

        if (driverService.findByEmail(driver.getEmail()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "A driver with this email already exists.");
            return "redirect:/drivers";
        }

        if (imageFile.isEmpty()) {
            result.rejectValue("name", "image.required", "An image file for the driver is required.");
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors below.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newDriverProfile", result);
            redirectAttributes.addFlashAttribute("newDriverProfile", driver);
            return "redirect:/drivers";
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            driver.setImage(base64Image);

            // *** THE FIX: Part 2 ***
            // If a bus ID was submitted, find the Bus object and assign it to the driver.
            if (assignedBusId != null) {
                Bus assignedBus = busService.getBusById(assignedBusId);
                driver.setAssignedBus(assignedBus);
            }
            
            driverService.saveDriver(driver);

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing image file. Please try again.");
            return "redirect:/drivers";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Driver profile added successfully!");
        return "redirect:/drivers";
    }
    
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Driver> driver = driverService.findById(id);
        if (driver.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Driver not found.");
            return "redirect:/drivers";
        }
        model.addAttribute("driver", driver.get());
        model.addAttribute("buses", busService.getAllBuses());
        return "driver-edit";
    }
    
    @PostMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String updateDriver(@PathVariable Long id,
                               @ModelAttribute("driver") Driver driver,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam(value = "assignedBusId", required = false) Long assignedBusId,
                               RedirectAttributes redirectAttributes) {
        try {
            Driver existingDriver = driverService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
            
            // Update fields
            existingDriver.setName(driver.getName());
            existingDriver.setEmail(driver.getEmail());
            existingDriver.setPhone(driver.getPhone());
            
            // Update image if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
                existingDriver.setImage(base64Image);
            }
            
            // Update bus assignment
            if (assignedBusId != null) {
                Bus assignedBus = busService.getBusById(assignedBusId);
                existingDriver.setAssignedBus(assignedBus);
            } else {
                existingDriver.setAssignedBus(null);
            }
            
            driverService.saveDriver(existingDriver);
            redirectAttributes.addFlashAttribute("successMessage", "Driver updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating driver: " + e.getMessage());
        }
        return "redirect:/drivers";
    }
    
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deleteDriver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            driverService.deleteDriver(id);
            redirectAttributes.addFlashAttribute("successMessage", "Driver deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting driver: " + e.getMessage());
        }
        return "redirect:/drivers";
    }
}
