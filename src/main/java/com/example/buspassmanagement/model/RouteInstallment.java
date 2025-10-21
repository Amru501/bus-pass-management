package com.example.buspassmanagement.model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the installment configuration for a specific bus route.
 * Admin can set 3 installments per route with amounts and deadlines.
 */
@Entity
@Table(name = "route_installments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteInstallment implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Route name is required.")
    @Column(nullable = false, unique = true)
    private String routeName;

    // First Installment
    @NotNull(message = "First installment amount is required.")
    @Min(value = 0, message = "Amount must be zero or positive.")
    @Column(nullable = false)
    private Double installment1Amount;

    @NotNull(message = "First installment deadline is required.")
    @Column(nullable = false)
    private LocalDate installment1Deadline;

    // Second Installment
    @NotNull(message = "Second installment amount is required.")
    @Min(value = 0, message = "Amount must be zero or positive.")
    @Column(nullable = false)
    private Double installment2Amount;

    @NotNull(message = "Second installment deadline is required.")
    @Column(nullable = false)
    private LocalDate installment2Deadline;

    // Third Installment
    @NotNull(message = "Third installment amount is required.")
    @Min(value = 0, message = "Amount must be zero or positive.")
    @Column(nullable = false)
    private Double installment3Amount;

    @NotNull(message = "Third installment deadline is required.")
    @Column(nullable = false)
    private LocalDate installment3Deadline;

    // Total fee for the route (sum of all installments)
    @Column(nullable = false)
    private Double totalFee;

    // Helper method to calculate total fee
    public void calculateTotalFee() {
        double amount1 = (installment1Amount != null ? installment1Amount : 0.0);
        double amount2 = (installment2Amount != null ? installment2Amount : 0.0);
        double amount3 = (installment3Amount != null ? installment3Amount : 0.0);
        this.totalFee = amount1 + amount2 + amount3;
    }
}

