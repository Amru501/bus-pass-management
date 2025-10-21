package com.example.buspassmanagement.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.buspassmanagement.model.BusPass;
import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.Payment.PaymentStatus;
import com.example.buspassmanagement.model.RouteInstallment;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.BusPassService;
import com.example.buspassmanagement.service.PaymentService;
import com.example.buspassmanagement.service.RouteInstallmentService;
import com.example.buspassmanagement.service.UserService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RouteInstallmentService routeInstallmentService;

    @Autowired
    private BusPassService busPassService;

    /**
     * Displays payment records.
     * - Admins see all payments.
     * - Users see only their own payments.
     * Includes robust error handling to prevent Whitelabel errors.
     */
    @GetMapping
    public String listPayments(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // Initialize summary variables for the user view.
        model.addAttribute("totalPaid", 0.0);
        model.addAttribute("totalPending", 0.0);

        try {
            System.out.println("DEBUG: Starting payment page load for user: " + principal.getName());
            
            User currentUser = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + principal.getName()));
            
            System.out.println("DEBUG: Found user: " + currentUser.getName() + " with role: " + currentUser.getRole());

            List<Payment> payments;
            if (currentUser.getRole() == User.Role.ADMIN) {
                System.out.println("DEBUG: Loading payments for ADMIN user");
                payments = paymentService.getAllPayments();
                model.addAttribute("payments", payments);
            } else {
                System.out.println("DEBUG: Loading payments for USER with ID: " + currentUser.getId());
                // This logic is now fully self-contained for ROLE_USER
                payments = paymentService.getPaymentsByUserId(currentUser.getId());
                System.out.println("DEBUG: Retrieved payments list: " + (payments != null ? payments.size() + " items" : "null"));
                
                // *** FIX FOR NEW USERS ***
                // Handle null or empty payments list for new users who haven't been assigned any installments yet
                if (payments == null) {
                    System.out.println("DEBUG: Payments was null, converting to empty list");
                    payments = Collections.emptyList();
                }
                
                // Using explicit lambdas instead of method references for maximum compatibility and clarity.
                // This avoids potential runtime issues and makes the calculation more direct.
                double totalPaid = payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.PAID)
                        .mapToDouble(p -> p.getAmount())
                        .sum();
                
                double totalPending = payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                        .mapToDouble(p -> p.getAmount())
                        .sum();
                
                System.out.println("DEBUG: Calculated totals - Paid: " + totalPaid + ", Pending: " + totalPending);
                
                model.addAttribute("totalPaid", totalPaid);
                model.addAttribute("totalPending", totalPending);
                model.addAttribute("payments", payments);
            }
            
            System.out.println("DEBUG: Successfully loaded payment page data");

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR loading payment page: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Stack trace:");
            e.printStackTrace();
            model.addAttribute("errorMessage", "Could not load payment details due to a server error: " + e.getMessage());
            model.addAttribute("payments", Collections.emptyList());
        }

        return "payment";
    }

    // NOTE: Manual fee schedule creation for individual students has been removed.
    // Fee schedules are now managed through route-based installments.
    // Admins should use /route-installments to configure installments per route.
    // Students select their routes and pay accordingly via /payments/installments.

    /**
     * USER: Marks a single pending installment as PAID.
     */
    @GetMapping("/pay/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String markPaymentAsPaid(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
            Payment payment = paymentService.getPaymentById(id).orElseThrow();

            if (!payment.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Unauthorized access to payment record.");
                return "redirect:/payments";
            }
            
            paymentService.markAsPaid(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment marked as paid!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing payment.");
        }
        return "redirect:/payments";
    }

    /**
     * USER: Pays the full outstanding balance.
     * This clears all PENDING payments and creates a single PAID record for the total sum.
     */
    @PostMapping("/pay-full")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String payFullFees(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
            paymentService.payFullAmountForUser(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully paid the full outstanding sum.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while processing the full payment.");
        }
        return "redirect:/payments";
    }

    /**
     * ADMIN: Deletes a payment record.
     */
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentService.deletePayment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Payment record deleted successfully.");
        return "redirect:/payments";
    }

    /**
     * USER: View available route installments and payment options
     */
    @GetMapping("/installments")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String viewInstallments(Model model, Principal principal) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Get all route installment configurations
            List<RouteInstallment> allRoutes = routeInstallmentService.findAll();
            model.addAttribute("routes", allRoutes);

            // Get user's bus pass
            BusPass busPass = busPassService.findByUser(currentUser).orElse(null);
            
            // Get user's selected route and payment status
            if (busPass != null && busPass.getSelectedRoute() != null) {
                RouteInstallment selectedRouteInstallment = routeInstallmentService
                    .findByRouteName(busPass.getSelectedRoute())
                    .orElse(null);
                model.addAttribute("selectedRoute", selectedRouteInstallment);
                
                // Get payment status
                PaymentService.PaymentStatusInfo paymentStatus = paymentService.getPaymentStatus(currentUser);
                model.addAttribute("paymentStatus", paymentStatus);
            }

            model.addAttribute("user", currentUser);
            model.addAttribute("busPass", busPass);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading installment information: " + e.getMessage());
            model.addAttribute("routes", Collections.emptyList());
        }

        return "payment-installments";
    }

    /**
     * USER: Select a route
     */
    @PostMapping("/select-route")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String selectRoute(@RequestParam("routeName") String routeName,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify route exists
            routeInstallmentService.findByRouteName(routeName)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));

            // Get or create bus pass for user
            BusPass busPass = busPassService.getOrCreateBusPass(currentUser);

            // Check if user already has payments for another route
            if (busPass.getSelectedRoute() != null && 
                !busPass.getSelectedRoute().equals(routeName)) {
                List<Payment> existingPayments = paymentService.getPaymentsByUserId(currentUser.getId())
                    .stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PAID)
                    .collect(Collectors.toList());
                
                if (!existingPayments.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "You already have payments for another route. Please contact admin to change routes.");
                    return "redirect:/payments/installments";
                }
            }

            // Set selected route on bus pass
            busPass.setSelectedRoute(routeName);
            busPassService.save(busPass);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Route selected successfully! You can now proceed with payments.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error selecting route: " + e.getMessage());
        }

        return "redirect:/payments/installments";
    }

    /**
     * USER: Pay a specific installment
     */
    @PostMapping("/pay-installment")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String payInstallment(@RequestParam("installmentNumber") Integer installmentNumber,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            BusPass busPass = busPassService.findByUser(currentUser).orElse(null);
            
            if (busPass == null || busPass.getSelectedRoute() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Please select a route first.");
                return "redirect:/payments/installments";
            }

            paymentService.payInstallment(currentUser, busPass.getSelectedRoute(), installmentNumber);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Installment " + installmentNumber + " paid successfully!");

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error processing payment: " + e.getMessage());
        }

        return "redirect:/payments/installments";
    }

    /**
     * USER: Pay all 3 installments together
     */
    @PostMapping("/pay-all-installments")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String payAllInstallments(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            BusPass busPass = busPassService.findByUser(currentUser).orElse(null);
            
            if (busPass == null || busPass.getSelectedRoute() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Please select a route first.");
                return "redirect:/payments/installments";
            }

            paymentService.payAllInstallments(currentUser, busPass.getSelectedRoute());
            redirectAttributes.addFlashAttribute("successMessage", 
                "All installments paid successfully! Your bus pass is now active.");

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error processing payment: " + e.getMessage());
        }

        return "redirect:/payments/installments";
    }
}

