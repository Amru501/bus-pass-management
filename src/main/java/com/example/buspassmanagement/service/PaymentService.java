package com.example.buspassmanagement.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.Payment.PaymentStatus;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
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
}
