package com.example.buspassmanagement.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor // Required for JPA/Hibernate
@AllArgsConstructor // Required for internal service logic
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JPA Relationship: A payment belongs to ONE User
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) 
    @NotNull(message = "User is required for payment.")
    private User user;

    @NotNull(message = "Amount is required.") 
    @Min(value = 0, message = "Amount must be zero or positive.") 
    @Column(nullable = false)
    private Double amount;

    @NotNull(message = "Due date is required.") 
    @Column(nullable = false)
    private LocalDate dueDate;

    // Status field (replaces boolean 'paid') to match database and business logic
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15) 
    private PaymentStatus status = PaymentStatus.PENDING; 

    // Route name for which this payment is made
    @Column(nullable = true)
    private String routeName;

    // Installment number (1, 2, or 3) - null means full payment
    @Column(nullable = true)
    private Integer installmentNumber;

    // Payment date - when the payment was actually made
    @Column(nullable = true)
    private LocalDate paymentDate;

    // Is this a full payment (all 3 installments together)?
    @Column(nullable = false)
    private Boolean isFullPayment = false;
    
    // Define the Enum for status
    public enum PaymentStatus {
        PENDING, PAID, OVERDUE, CANCELLED
    }
}
