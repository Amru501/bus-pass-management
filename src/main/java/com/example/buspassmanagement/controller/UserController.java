package com.example.buspassmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import for flash messages

import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    
    // Note: HomeController should exist and map "/" to "home" to avoid errors.

    // 1. SHOW REGISTRATION FORM (GET /register)
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Ensure an empty user object is always available for the form binding
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register"; // Points to templates/register.html
    }

    // 2. PROCESS REGISTRATION (POST /register)
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               @RequestParam(value = "securityKey", required = false) String securityKey,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        // Security key for admin registration
        final String ADMIN_SECURITY_KEY = "Amru@1234";
        
        // 1. Handle Validation Errors (e.g., password empty, name empty)
        if (result.hasErrors()) {
            // Returns to the register form, retaining error messages
            return "register"; 
        }

        // 2. Handle Admin Security Key Validation
        if (user.getRole() == User.Role.ADMIN) {
            if (securityKey == null || securityKey.trim().isEmpty()) {
                model.addAttribute("securityKeyError", "Security key is required for admin registration.");
                return "register";
            }
            if (!ADMIN_SECURITY_KEY.equals(securityKey)) {
                model.addAttribute("securityKeyError", "Invalid security key. Access denied.");
                return "register";
            }
        }

        // 3. Handle Email Duplication Error
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            // Adds the error message required by register.html
            model.addAttribute("emailError", "An account with this email already exists.");
            // Keep the user object in the model to retain other input fields
            return "register"; 
        }

        // 4. Save User (Password Hashing should happen in UserService)
        userService.saveUser(user); // Assuming this is the method that calls the repository and hashes the password
        
        // 5. Redirect to Login with success message
        String roleMessage = (user.getRole() == User.Role.ADMIN) ? "Admin" : "Student";
        redirectAttributes.addFlashAttribute("successMessage", 
            roleMessage + " registration successful! Please log in.");
        return "redirect:/login";
    }

    // 3. SHOW LOGIN PAGE (GET /login)
    @GetMapping("/login")
    public String login() {
        return "login"; // Points to templates/login.html
    }
}
