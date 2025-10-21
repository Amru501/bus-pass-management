package com.example.buspassmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Recommended to add @Repository

import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.User; // ⬅️ NEW IMPORT

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // 🔑 Correct way to query by the related User object:
    // The field in Payment.java is named 'user', so the query method is 'findByUser'.
    List<Payment> findByUser(User user);

    // Alternative: Find payments by the User's ID (useful if you only have the ID)
    List<Payment> findByUser_Id(Long userId);
    
    // Find payments by user and route name
    List<Payment> findByUserAndRouteName(User user, String routeName);
    
    // Find payments by user, route name, and installment number
    List<Payment> findByUserAndRouteNameAndInstallmentNumber(User user, String routeName, Integer installmentNumber);
    
    // Find paid payments by user and route name
    List<Payment> findByUserAndRouteNameAndStatus(User user, String routeName, Payment.PaymentStatus status);
}