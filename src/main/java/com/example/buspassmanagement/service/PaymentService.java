package com.example.buspassmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.Payment.PaymentStatus; // IMPORT PaymentStatus
import com.example.buspassmanagement.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        // Assuming you have findAllByOrderByTimestampDesc() or a similar sort for display:
        return paymentRepository.findAll(); 
    }

    // Assuming you updated the repository method to find by user ID:
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUser_Id(userId);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    // 🔑 FIX: Update logic to set STATUS instead of the old 'paid' boolean
    public Payment markAsPaid(Long id) {
        return paymentRepository.findById(id).map(payment -> {
            payment.setStatus(PaymentStatus.PAID); // <-- Set the new status
            return paymentRepository.save(payment);
        }).orElse(null); // Or throw a custom exception
    }
    
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
