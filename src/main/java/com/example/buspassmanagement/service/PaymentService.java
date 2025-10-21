package com.example.buspassmanagement.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.buspassmanagement.model.BusPass;
import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.Payment.PaymentStatus;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RouteInstallmentService routeInstallmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private BusPassService busPassService;

    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        List<Payment> allPayments = paymentRepository.findAll();
        
        // Filter out payments with missing/deleted users and initialize user data
        return allPayments.stream()
            .filter(payment -> {
                try {
                    // Try to access the user - this will trigger lazy loading
                    // Also access the name to fully initialize the user object
                    payment.getUser().getId();
                    payment.getUser().getName(); // Initialize the user proxy
                    return true;
                } catch (Exception e) {
                    // User doesn't exist - log and filter out this payment
                    System.err.println("Warning: Payment ID " + payment.getId() + 
                        " references a non-existent user. Skipping from results.");
                    return false;
                }
            })
            .collect(Collectors.toList());
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUser_Id(userId);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Transactional
    public Payment markAsPaid(Long id) {
        return paymentRepository.findById(id).map(payment -> {
            payment.setStatus(PaymentStatus.PAID);
            return paymentRepository.save(payment);
        }).orElse(null);
    }
    
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    /**
     * Handles the logic for a user paying their full outstanding balance.
     * This method is transactional, ensuring all operations succeed or none do.
     */
    @Transactional
    public void payFullAmountForUser(User user) {
        List<Payment> pendingPayments = getPaymentsByUserId(user.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .collect(Collectors.toList());

        if (pendingPayments.isEmpty()) {
            throw new IllegalStateException("You have no outstanding fees to pay.");
        }

        double totalAmount = pendingPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        
        pendingPayments.forEach(p -> deletePayment(p.getId()));

        Payment lumpSumPayment = new Payment();
        lumpSumPayment.setAmount(totalAmount);
        lumpSumPayment.setDueDate(LocalDate.now());
        lumpSumPayment.setStatus(PaymentStatus.PAID);
        lumpSumPayment.setUser(user);
        addPayment(lumpSumPayment);
    }

    /**
     * Pay a specific installment for a route
     */
    @Transactional
    public void payInstallment(User user, String routeName, Integer installmentNumber) {
        // Check if already paid
        List<Payment> existingPayments = paymentRepository.findByUserAndRouteNameAndInstallmentNumber(
            user, routeName, installmentNumber);
        
        boolean alreadyPaid = existingPayments.stream()
            .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);
        
        if (alreadyPaid) {
            throw new IllegalStateException("This installment has already been paid.");
        }

        // Get route installment configuration
        var routeInstallment = routeInstallmentService.findByRouteName(routeName)
            .orElseThrow(() -> new IllegalStateException("Route installment configuration not found."));

        // Determine amount and deadline based on installment number
        Double amount;
        LocalDate deadline;
        switch (installmentNumber) {
            case 1:
                amount = routeInstallment.getInstallment1Amount();
                deadline = routeInstallment.getInstallment1Deadline();
                break;
            case 2:
                amount = routeInstallment.getInstallment2Amount();
                deadline = routeInstallment.getInstallment2Deadline();
                break;
            case 3:
                amount = routeInstallment.getInstallment3Amount();
                deadline = routeInstallment.getInstallment3Deadline();
                break;
            default:
                throw new IllegalArgumentException("Invalid installment number. Must be 1, 2, or 3.");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setRouteName(routeName);
        payment.setInstallmentNumber(installmentNumber);
        payment.setAmount(amount);
        payment.setDueDate(deadline);
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(PaymentStatus.PAID);
        payment.setIsFullPayment(false);
        
        paymentRepository.save(payment);

        // Update user's selected route and check if all installments paid
        busPassService.setSelectedRoute(user, routeName);
        checkAndActivateBusPass(user, routeName);
    }

    /**
     * Pay all 3 installments together
     */
    @Transactional
    public void payAllInstallments(User user, String routeName) {
        // Check if any installments already paid
        List<Payment> existingPayments = paymentRepository.findByUserAndRouteName(user, routeName);
        boolean anyPaid = existingPayments.stream()
            .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);
        
        if (anyPaid) {
            throw new IllegalStateException("Some installments have already been paid. Cannot pay all together.");
        }

        // Get route installment configuration
        var routeInstallment = routeInstallmentService.findByRouteName(routeName)
            .orElseThrow(() -> new IllegalStateException("Route installment configuration not found."));

        // Create single payment for all installments
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setRouteName(routeName);
        payment.setAmount(routeInstallment.getTotalFee());
        payment.setDueDate(routeInstallment.getInstallment3Deadline());
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(PaymentStatus.PAID);
        payment.setIsFullPayment(true);
        payment.setInstallmentNumber(null); // null indicates full payment
        
        paymentRepository.save(payment);

        // Update user's selected route and activate bus pass
        busPassService.setSelectedRoute(user, routeName);
        busPassService.activatePass(user);
    }

    /**
     * Check if all installments are paid and activate bus pass if so
     */
    private void checkAndActivateBusPass(User user, String routeName) {
        List<Payment> paidPayments = paymentRepository.findByUserAndRouteNameAndStatus(
            user, routeName, PaymentStatus.PAID);
        
        // Check if full payment exists
        boolean hasFullPayment = paidPayments.stream()
            .anyMatch(p -> p.getIsFullPayment() != null && p.getIsFullPayment());
        
        if (hasFullPayment) {
            busPassService.activatePass(user);
            return;
        }

        // Check if all 3 installments are paid
        boolean hasInstallment1 = paidPayments.stream()
            .anyMatch(p -> p.getInstallmentNumber() != null && p.getInstallmentNumber() == 1);
        boolean hasInstallment2 = paidPayments.stream()
            .anyMatch(p -> p.getInstallmentNumber() != null && p.getInstallmentNumber() == 2);
        boolean hasInstallment3 = paidPayments.stream()
            .anyMatch(p -> p.getInstallmentNumber() != null && p.getInstallmentNumber() == 3);
        
        if (hasInstallment1 && hasInstallment2 && hasInstallment3) {
            busPassService.activatePass(user);
        }
    }

    /**
     * Get payment status for a user's route
     */
    public PaymentStatusInfo getPaymentStatus(User user) {
        // Get user's bus pass
        BusPass busPass = busPassService.findByUser(user).orElse(null);
        
        if (busPass == null || busPass.getSelectedRoute() == null) {
            return new PaymentStatusInfo(false, false, false, false, false);
        }

        List<Payment> paidPayments = paymentRepository.findByUserAndRouteNameAndStatus(
            user, busPass.getSelectedRoute(), PaymentStatus.PAID);
        
        boolean hasFullPayment = paidPayments.stream()
            .anyMatch(p -> p.getIsFullPayment() != null && p.getIsFullPayment());
        
        boolean hasInstallment1 = paidPayments.stream()
            .anyMatch(p -> p.getInstallmentNumber() != null && p.getInstallmentNumber() == 1);
        boolean hasInstallment2 = paidPayments.stream()
            .anyMatch(p -> p.getInstallmentNumber() != null && p.getInstallmentNumber() == 2);
        boolean hasInstallment3 = paidPayments.stream()
            .anyMatch(p -> p.getInstallmentNumber() != null && p.getInstallmentNumber() == 3);
        
        return new PaymentStatusInfo(
            hasFullPayment,
            hasInstallment1,
            hasInstallment2,
            hasInstallment3,
            busPass.isActive()
        );
    }

    /**
     * Inner class to hold payment status information
     */
    public static class PaymentStatusInfo {
        public final boolean hasFullPayment;
        public final boolean hasInstallment1;
        public final boolean hasInstallment2;
        public final boolean hasInstallment3;
        public final boolean busPassActive;

        public PaymentStatusInfo(boolean hasFullPayment, boolean hasInstallment1, 
                                boolean hasInstallment2, boolean hasInstallment3,
                                boolean busPassActive) {
            this.hasFullPayment = hasFullPayment;
            this.hasInstallment1 = hasInstallment1;
            this.hasInstallment2 = hasInstallment2;
            this.hasInstallment3 = hasInstallment3;
            this.busPassActive = busPassActive;
        }
    }
}
