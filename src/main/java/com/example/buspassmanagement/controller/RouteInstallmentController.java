package com.example.buspassmanagement.controller;

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

import com.example.buspassmanagement.model.RouteInstallment;
import com.example.buspassmanagement.service.BusService;
import com.example.buspassmanagement.service.RouteInstallmentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/route-installments")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class RouteInstallmentController {

    @Autowired
    private RouteInstallmentService routeInstallmentService;

    @Autowired
    private BusService busService;

    /**
     * Display all route installment configurations
     */
    @GetMapping
    public String showRouteInstallments(Model model) {
        List<RouteInstallment> installments = routeInstallmentService.findAll();
        model.addAttribute("installments", installments);
        return "route-installments";
    }

    /**
     * Show form to add a new route installment configuration
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("routeInstallment", new RouteInstallment());
        model.addAttribute("buses", busService.getAllBuses());
        return "route-installment-add";
    }

    /**
     * Process the form to add a new route installment configuration
     */
    @PostMapping("/add")
    public String addRouteInstallment(@Valid @ModelAttribute("routeInstallment") RouteInstallment routeInstallment,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("buses", busService.getAllBuses());
            return "route-installment-add";
        }

        // Check if route already has installment configuration
        if (routeInstallmentService.existsByRouteName(routeInstallment.getRouteName())) {
            redirectAttributes.addFlashAttribute("error", 
                "Installment configuration already exists for this route. Please edit instead.");
            return "redirect:/route-installments";
        }

        routeInstallmentService.save(routeInstallment);
        redirectAttributes.addFlashAttribute("success", 
            "Route installment configuration added successfully!");
        return "redirect:/route-installments";
    }

    /**
     * Show form to edit an existing route installment configuration
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        RouteInstallment routeInstallment = routeInstallmentService.findById(id)
            .orElse(null);
        
        if (routeInstallment == null) {
            redirectAttributes.addFlashAttribute("error", "Route installment configuration not found.");
            return "redirect:/route-installments";
        }

        model.addAttribute("routeInstallment", routeInstallment);
        model.addAttribute("buses", busService.getAllBuses());
        return "route-installment-edit";
    }

    /**
     * Process the form to update an existing route installment configuration
     */
    @PostMapping("/edit/{id}")
    public String updateRouteInstallment(@PathVariable Long id,
                                        @Valid @ModelAttribute("routeInstallment") RouteInstallment routeInstallment,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (result.hasErrors()) {
            model.addAttribute("buses", busService.getAllBuses());
            return "route-installment-edit";
        }

        routeInstallment.setId(id);
        routeInstallmentService.save(routeInstallment);
        redirectAttributes.addFlashAttribute("success", 
            "Route installment configuration updated successfully!");
        return "redirect:/route-installments";
    }

    /**
     * Delete a route installment configuration
     */
    @PostMapping("/delete/{id}")
    public String deleteRouteInstallment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            routeInstallmentService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", 
                "Route installment configuration deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting route installment configuration: " + e.getMessage());
        }
        return "redirect:/route-installments";
    }
}


