package com.example.buspassmanagement.controller;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            // This is the standard, direct way to set the image data.
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            driver.setImage(base64Image);
            
            driverService.saveDriver(driver);

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing image file. Please try again.");
            return "redirect:/drivers";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Driver profile added successfully!");
        return "redirect:/drivers";
    }
}

