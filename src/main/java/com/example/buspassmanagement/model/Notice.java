package com.example.buspassmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Notice message cannot be empty.")
    @Column(nullable = false, columnDefinition = "TEXT") // Use TEXT for potentially longer messages
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // 🔑 Relationship 1: The bus this notice pertains to (e.g., "Bus A is late")
    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    @NotNull(message = "Notice must be assigned to a bus.")
    private Bus bus;
    
    // 🔑 Relationship 2: The User (Admin/Driver) who posted the notice (for auditing)
    @ManyToOne
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    @NotNull(message = "Notice must be attributed to a user.")
    private User postedBy;
}