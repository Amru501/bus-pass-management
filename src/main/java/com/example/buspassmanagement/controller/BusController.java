package com.example.buspassmanagement.controller;

import java.util.Collections;
import java.util.List;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.service.BusService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/buses")
public class BusController {

    @Autowired
    private BusService busService;

    /**
     * Displays the list of all buses. Includes error handling.
     * Accessible to all authenticated users.
     */
    @GetMapping
    public String listBuses(Model model) {
        try {
            List<Bus> buses = busService.getAllBuses();
            model.addAttribute("buses", buses);
            
            // Calculate total seats
            int totalSeats = buses.stream()
                .mapToInt(Bus::getSeats)
                .sum();
            model.addAttribute("totalSeats", totalSeats);
            
            if (!model.containsAttribute("bus")) {
                model.addAttribute("bus", new Bus());
            }
        } catch (Exception e) {
            System.err.println("ERROR loading bus list: " + e.getMessage());
            model.addAttribute("errorMessage", "Could not load bus information. Please contact support.");
            model.addAttribute("buses", Collections.emptyList());
            model.addAttribute("totalSeats", 0);
        }
        return "buses";
    }

    /**
     * Processes adding a new bus. Admin only.
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String addBus(@Valid @ModelAttribute("bus") Bus bus,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.bus", result);
            redirectAttributes.addFlashAttribute("bus", bus);
            return "redirect:/buses";
        }
        try {
            busService.saveBus(bus);
            redirectAttributes.addFlashAttribute("successMessage", "Bus added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding bus: " + e.getMessage());
        }
        return "redirect:/buses";
    }

    /**
     * Shows the form to edit a bus. Admin only.
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Bus bus = busService.getBusById(id);
        if (bus == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bus not found with ID: " + id);
            return "redirect:/buses";
        }
        model.addAttribute("bus", bus);
        return "bus-edit";
    }
    
    /**
     * Processes the update of a bus. Admin only.
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String updateBus(@Valid @ModelAttribute("bus") Bus bus,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "bus-edit";
        }
        busService.saveBus(bus);
        redirectAttributes.addFlashAttribute("successMessage", "Bus updated successfully!");
        return "redirect:/buses";
    }

    /**
     * Deletes a bus. Admin only.
     */
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deleteBus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Bus bus = busService.getBusById(id);
            if (bus == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bus not found.");
                return "redirect:/buses";
            }
            
            // Delete all related entities first
            // 1. Delete all drivers assigned to this bus
            busService.deleteDriversForBus(id);
            
            // 2. Delete all notices for this bus
            busService.deleteNoticesForBus(id);
            
            busService.deleteBus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bus deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting bus: " + e.getMessage());
        }
        return "redirect:/buses";
    }
}

