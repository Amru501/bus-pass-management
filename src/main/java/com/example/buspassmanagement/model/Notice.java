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
    @Column(nullable = false, columnDefinition = "TEXT") 
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // *** FIX APPLIED HERE ***
    // Removed @NotNull validation. The controller logic is responsible for assigning this object
    // before saving, and the database constraint `nullable = false` provides the final integrity check.
    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;
    
    // *** FIX APPLIED HERE ***
    // Removed @NotNull validation for the same reason as above.
    @ManyToOne
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    private User postedBy;
}
