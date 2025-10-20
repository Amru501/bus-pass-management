package com.example.buspassmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.Payment.PaymentStatus;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.PaymentService;
import com.example.buspassmanagement.service.UserService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

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

    /**
     * ADMIN: Shows the form to create a new fee schedule for a student.
     * Provides a list of all students (users) to the form.
     */
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String showAddPaymentForm(Model model) {
        List<User> students = userService.getAllUsers().stream()
                .filter(user -> user.getRole() == User.Role.USER)
                .collect(Collectors.toList());
        model.addAttribute("students", students);
        return "payment-add";
    }

    /**
     * ADMIN: Processes the creation of a new 3-installment fee schedule for a specific user.
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String createFeeSchedule(@RequestParam("userId") Long userId,
                                    @RequestParam("inst1Amount") Double inst1Amount,
                                    @RequestParam("inst1DueDate") String inst1DueDateStr,
                                    @RequestParam("inst2Amount") Double inst2Amount,
                                    @RequestParam("inst2DueDate") String inst2DueDateStr,
                                    @RequestParam("inst3Amount") Double inst3Amount,
                                    @RequestParam("inst3DueDate") String inst3DueDateStr,
                                    RedirectAttributes redirectAttributes) {
        try {
            User student = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found for ID: " + userId));

            Payment inst1 = new Payment(null, student, inst1Amount, LocalDate.parse(inst1DueDateStr), PaymentStatus.PENDING);
            Payment inst2 = new Payment(null, student, inst2Amount, LocalDate.parse(inst2DueDateStr), PaymentStatus.PENDING);
            Payment inst3 = new Payment(null, student, inst3Amount, LocalDate.parse(inst3DueDateStr), PaymentStatus.PENDING);

            paymentService.addPayment(inst1);
            paymentService.addPayment(inst2);
            paymentService.addPayment(inst3);

            redirectAttributes.addFlashAttribute("successMessage", "Fee schedule successfully created for " + student.getName() + ".");

        } catch (IllegalArgumentException | DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create schedule. Reason: " + e.getMessage());
            return "redirect:/payments/add";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected server error occurred.");
            return "redirect:/payments/add";
        }
        return "redirect:/payments";
    }

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
}

