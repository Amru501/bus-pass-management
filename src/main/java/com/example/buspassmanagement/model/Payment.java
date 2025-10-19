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
    @ManyToOne 
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
    
    // Define the Enum for status
    public enum PaymentStatus {
        PENDING, PAID, OVERDUE, CANCELLED
    }
}
