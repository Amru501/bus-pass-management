package com.example.buspassmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

    // **1. LIST ALL BUSES** (Read All)
    @GetMapping // Maps to /buses
    public String listBuses(Model model) {
        // 1. Fetch all buses for the table list
        model.addAttribute("buses", busService.getAllBuses());
        
        // 2. Ensure an empty Bus object is available for the ADD form (Crucial for initial GET request)
        if (!model.containsAttribute("bus")) {
            model.addAttribute("bus", new Bus());
        }
        
        return "buses"; // Points to templates/buses.html
    }

    // **2. PROCESS ADD/CREATE** @PostMapping("/add")
    @PostMapping("/add")
    public String addBus(@Valid @ModelAttribute("bus") Bus bus, 
                         BindingResult result, 
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (result.hasErrors()) {
            // Flash the errors and the bus object back to the list page via redirect
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.bus", result);
            redirectAttributes.addFlashAttribute("bus", bus);
            return "redirect:/buses"; 
        }

        busService.saveBus(bus);
        redirectAttributes.addFlashAttribute("successMessage", "Bus added successfully!");
        return "redirect:/buses";
    }

    // **3. SHOW EDIT FORM** (Read One)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Bus bus = busService.getBusById(id);
        if (bus == null) {
             // If bus not found, redirect back to list
             return "redirect:/buses"; 
        }
        model.addAttribute("bus", bus);
        return "bus-edit"; // Points to templates/bus-edit.html
    }
    
    // **4. PROCESS UPDATE/SAVE**
    @PostMapping("/update")
    public String updateBus(@Valid @ModelAttribute("bus") Bus bus, 
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
             // If validation fails, return the user back to the edit form directly (NOT REDIRECT)
             return "bus-edit"; 
        }

        busService.saveBus(bus);
        redirectAttributes.addFlashAttribute("successMessage", "Bus updated successfully!");
        return "redirect:/buses";
    }


    // **5. DELETE BUS**
    @GetMapping("/delete/{id}")
    public String deleteBus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        busService.deleteBus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Bus deleted successfully.");
        return "redirect:/buses";
    }
}
