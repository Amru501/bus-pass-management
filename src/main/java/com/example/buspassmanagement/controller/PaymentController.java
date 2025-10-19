package com.example.buspassmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.Payment.PaymentStatus; 
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.BusService;
import com.example.buspassmanagement.service.PaymentService;
import com.example.buspassmanagement.service.UserService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private BusService busService; 

    // 1. LIST PAYMENTS (Role-Based View)
    @GetMapping
    public String listPayments(Model model, Principal principal) {
        List<Payment> payments = Collections.emptyList();
        User currentUser = null;
        String errorMessage = null;

        if (principal != null) {
            currentUser = userService.findByEmail(principal.getName()).orElse(null);

            if (currentUser != null) {
                try {
                    if (currentUser.getRole() == User.Role.ADMIN) {
                        payments = paymentService.getAllPayments();
                    } else {
                        payments = paymentService.getPaymentsByUserId(currentUser.getId());
                    }
                } catch (DataAccessException e) {
                    System.err.println("Database Error fetching payments: " + e.getMessage());
                    errorMessage = "Database Error: Could not load payments. Check server logs for schema issues.";
                }
            }
        }
        
        model.addAttribute("payments", payments);
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage); 
        }
        
        if (currentUser != null && currentUser.getRole() == User.Role.ADMIN) {
             model.addAttribute("users", userService.getAllUsers());
        }
        return "payment";
    }

    // 2. ADMIN: SHOW ADD/SCHEDULE FORM
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String addPaymentForm(Model model) {
        model.addAttribute("buses", busService.getAllBuses()); 
        return "payment-add";
    }

    // 3. ADMIN: PROCESS FEE SCHEDULE GENERATION
    @PostMapping("/schedule")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String generateFeeSchedule(
            @RequestParam(value = "routeId", required = true) String routeIdValue, 
            @RequestParam("inst1Amount") Double inst1Amount,
            @RequestParam("inst1DueDate") String inst1DueDateStr,
            @RequestParam("inst2Amount") Double inst2Amount,
            @RequestParam("inst2DueDate") String inst2DueDateStr,
            @RequestParam("inst3Amount") Double inst3Amount,
            @RequestParam("inst3DueDate") String inst3DueDateStr,
            RedirectAttributes redirectAttributes) {

        try {
            Bus targetBus = null;
            Long routeId;

            // 1. Handle "ALL" or single route conversion
            if (!"ALL".equalsIgnoreCase(routeIdValue)) {
                
                // Route ID Parsing (Catches if routeIdValue is not a number)
                routeId = Long.parseLong(routeIdValue); 
                targetBus = busService.getBusById(routeId);
                
                if (targetBus == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Error: Target route/bus not found.");
                    return "redirect:/payments";
                }
            }
            
            // --- Manual Type Conversion (Date) ---
            LocalDate inst1DueDate = LocalDate.parse(inst1DueDateStr);
            LocalDate inst2DueDate = LocalDate.parse(inst2DueDateStr);
            LocalDate inst3DueDate = LocalDate.parse(inst3DueDateStr);
            
            // 2. Find all users (Students) who need a fee schedule
            List<User> allUsers = userService.getAllUsers(); 
            
            if (allUsers.isEmpty()) {
                 redirectAttributes.addFlashAttribute("errorMessage", "No users found in the system to assign fees to.");
                 return "redirect:/payments";
            }

            // 3. Define the three installments template objects
            Payment inst1Template = new Payment();
            inst1Template.setAmount(inst1Amount);
            inst1Template.setDueDate(inst1DueDate);
            inst1Template.setStatus(PaymentStatus.PENDING); 

            Payment inst2Template = new Payment();
            inst2Template.setAmount(inst2Amount);
            inst2Template.setDueDate(inst2DueDate);
            inst2Template.setStatus(PaymentStatus.PENDING);
            
            Payment inst3Template = new Payment();
            inst3Template.setAmount(inst3Amount);
            inst3Template.setDueDate(inst3DueDate);
            inst3Template.setStatus(PaymentStatus.PENDING);

            List<Payment> installments = List.of(inst1Template, inst2Template, inst3Template);

            int totalPaymentsCreated = 0;
            
            // 4. Determine target users and buses for creation
            List<Bus> targetBuses;
            String targetRouteDisplay;
            
            if ("ALL".equalsIgnoreCase(routeIdValue)) {
                targetBuses = busService.getAllBuses();
                targetRouteDisplay = "ALL ROUTES";
            } else {
                targetBuses = (targetBus != null) ? List.of(targetBus) : Collections.emptyList();
                targetRouteDisplay = (targetBus != null) ? targetBus.getBusNumber() : "Unknown Bus";
            }

            for (User user : allUsers) {
                if (user.getRole() == User.Role.USER) { 
                    for (Bus bus : targetBuses) { 
                        for (Payment installment : installments) {
                            Payment newPayment = new Payment();
                            newPayment.setAmount(installment.getAmount());
                            newPayment.setDueDate(installment.getDueDate());
                            newPayment.setStatus(PaymentStatus.PENDING);
                            newPayment.setUser(user);
                            
                            paymentService.addPayment(newPayment);
                            totalPaymentsCreated++;
                        }
                    }
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Successfully created %d installment records for %s.", 
                              totalPaymentsCreated, targetRouteDisplay));
                              
        } catch (NumberFormatException | DateTimeParseException e) {
            // Catches invalid route ID, malformed amount, or date string
            System.err.println("Input/Parsing Error during schedule generation: " + e.getClass().getName() + " - " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to generate schedule: Amounts or Dates are invalid.");
            return "redirect:/payments/add"; 
        } catch (Exception e) {
            // Final catch-all for any unhandled exceptions
            System.err.println("Unexpected Server Error during schedule generation: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected server error occurred during scheduling.");
            return "redirect:/payments/add"; 
        }
        
        return "redirect:/payments";
    }

    // 4. USER: MARK AS PAID (Access control handled by logic)
    @GetMapping("/pay/{id}")
    @PreAuthorize("isAuthenticated()")
    public String markPaymentAsPaid(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByEmail(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Payment payment = paymentService.getPaymentById(id).orElse(null);
        
        // Security Check: Ensure user only pays their own bill, and payment exists.
        if (payment == null || !payment.getUser().getId().equals(currentUser.getId())) {
             redirectAttributes.addFlashAttribute("errorMessage", "Error: Unauthorized access to payment record.");
             return "redirect:/payments"; // Exit on unauthorized or not found
        }
        
        // Update status to PAID and save
        payment.setStatus(PaymentStatus.PAID);
        paymentService.addPayment(payment);
        redirectAttributes.addFlashAttribute("successMessage", "Payment marked as paid!");
        
        return "redirect:/payments"; // Final redirect for success
    }
    
    // 5. USER: PAY FULL SUM (Lump Sum Payment)
    @PostMapping("/pay-full")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String payFullFees(Principal principal, RedirectAttributes redirectAttributes) {
        
        User currentUser = userService.findByEmail(principal.getName()).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User session expired.");
            return "redirect:/login";
        }
        
        // 1. Get all PENDING payments for the user
        List<Payment> pendingPayments = paymentService.getPaymentsByUserId(currentUser.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .toList();

        if (pendingPayments.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You have no outstanding fees to pay.");
            return "redirect:/payments";
        }

        // 2. Calculate total amount
        double totalAmount = pendingPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        
        // 3. Delete all pending payments
        pendingPayments.forEach(p -> paymentService.deletePayment(p.getId()));

        // 4. Create one new "PAID" record for the full sum
        Payment lumpSumPayment = new Payment();
        lumpSumPayment.setAmount(totalAmount);
        lumpSumPayment.setDueDate(LocalDate.now());
        lumpSumPayment.setStatus(PaymentStatus.PAID); 
        lumpSumPayment.setUser(currentUser);
        paymentService.addPayment(lumpSumPayment);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            String.format("Successfully paid the full sum of ₹%.2f. All installments marked complete.", totalAmount));
        
        return "redirect:/payments";
    }

    // 6. ADMIN: DELETE PAYMENT (Restricted to ADMIN)
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentService.deletePayment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully.");
        return "redirect:/payments";
    }
}
