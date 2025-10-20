package com.example.buspassmanagement.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/edit")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String showEditProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", currentUser);
        return "profile-edit";
    }

    @PostMapping("/edit")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String updateProfile(@RequestParam("name") String name,
                               @RequestParam("phone") String phone,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if phone is already taken by another user
            if (!phone.equals(currentUser.getPhone())) {
                var existingUser = userService.getAllUsers().stream()
                        .filter(u -> phone.equals(u.getPhone()) && !u.getId().equals(currentUser.getId()))
                        .findFirst();
                
                if (existingUser.isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Phone number is already in use by another account.");
                    return "redirect:/profile/edit";
                }
            }
            
            currentUser.setName(name);
            currentUser.setPhone(phone);
            userService.saveUser(currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}
