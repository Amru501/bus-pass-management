package com.example.buspassmanagement.controller;

import java.security.Principal;
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
import com.example.buspassmanagement.model.Notice;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.BusService;
import com.example.buspassmanagement.service.NoticeService;
import com.example.buspassmanagement.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BusService busService;

    // 1. VIEW NOTICES (Restricted to ROLE_USER by SecurityConfig)
    @GetMapping
    public String listNotices(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "notices"; // Points to templates/notices.html
    }

    // 2. SHOW ADD NOTICE FORM (Restricted to Admin/Driver)
    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DRIVER')")
    public String addNoticeForm(Model model) {
        model.addAttribute("notice", new Notice());
        // Provide the list of buses for the dropdown
        model.addAttribute("buses", busService.getAllBuses()); 
        return "notices-add"; // Points to templates/notices-add.html
    }

    // 3. PROCESS ADD NOTICE (Restricted to Admin/Driver)
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DRIVER')")
    public String saveNotice(@Valid @ModelAttribute("notice") Notice notice,
                             BindingResult result,
                             Principal principal,
                             RedirectAttributes redirectAttributes,
                             Model model) { 
        
        // 1. Check if the message content is valid. We skip the bus check here.
        if (result.hasFieldErrors("message")) {
            // Re-fetch buses and return to the form to show errors
            model.addAttribute("buses", busService.getAllBuses());
            return "notices-add"; 
        }
        
        // 2. Get the poster
        User poster = userService.findByEmail(principal.getName()).orElse(null);
        if (poster == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: User not found in system.");
            return "redirect:/notices";
        }
        
        // --- START FIX for Null Pointer Access ---
        // Get the ID value submitted by the form. 
        // This ID will be null if "-- Select a Bus/Route --" was chosen.
        // It will be the string "ALL" if "ALL BUSES" was chosen.
        String busIdValue = (notice.getBus() != null && notice.getBus().getId() != null) 
                            ? String.valueOf(notice.getBus().getId()) 
                            : null;
        
        if (busIdValue == null || busIdValue.isEmpty()) {
             // Case: User submitted with "--- Select a Bus/Route ---" selected (ID is null/empty)
             // We only hit this if validation for message passed but bus selection failed.
             redirectAttributes.addFlashAttribute("errorMessage", "Please select a specific bus or 'ALL BUSES'.");
             redirectAttributes.addFlashAttribute("notice", notice);
             return "redirect:/notices/add";
        }

        if ("ALL".equals(busIdValue)) {
            // Option 3A: Send to ALL buses
            List<Bus> allBuses = busService.getAllBuses();
            for (Bus bus : allBuses) {
                Notice newNotice = new Notice();
                newNotice.setMessage(notice.getMessage());
                newNotice.setPostedBy(poster);
                newNotice.setBus(bus); // Set the specific bus object
                noticeService.addNotice(newNotice); // Save individual notice
            }
            redirectAttributes.addFlashAttribute("successMessage", "Notice successfully posted to all " + allBuses.size() + " routes.");

        } else {
            // Option 3B: Send to a single specific bus (busIdValue is now a valid ID string)
            try {
                Long busId = Long.valueOf(busIdValue);
                Bus targetBus = busService.getBusById(busId);
                
                if (targetBus != null) {
                    // Ensure the entire bus object is set correctly before saving
                    notice.setBus(targetBus); 
                    notice.setPostedBy(poster);
                    noticeService.addNotice(notice);
                    redirectAttributes.addFlashAttribute("successMessage", "Notice successfully posted to " + targetBus.getBusNumber() + ".");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Error: Target bus not found.");
                }
            } catch (NumberFormatException e) {
                 redirectAttributes.addFlashAttribute("errorMessage", "Invalid bus ID submitted.");
            }
        }
        // --- END FIX for Null Pointer Access ---
        
        return "redirect:/notices";
    }

    // 4. DELETE NOTICE (Restricted to Admin/Driver)
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DRIVER')")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        noticeService.deleteNotice(id);
        redirectAttributes.addFlashAttribute("successMessage", "Notice deleted successfully.");
        return "redirect:/notices";
    }
}
