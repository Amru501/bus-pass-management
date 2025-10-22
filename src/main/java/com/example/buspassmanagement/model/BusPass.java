package com.example.buspassmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a bus pass for a student.
 * Each user can have one bus pass.
 */
@Entity
@Table(name = "passes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusPass implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one relationship with User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Selected route for this bus pass
    @Column(nullable = true)
    private String selectedRoute;

    // Pass status - ACTIVE or INACTIVE (stored as string, not binary)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PassStatus status = PassStatus.INACTIVE;

    /**
     * Enum for pass status - more readable than boolean
     */
    public enum PassStatus {
        ACTIVE,
        INACTIVE
    }

    /**
     * Helper method to check if pass is active
     */
    public boolean isActive() {
        return this.status == PassStatus.ACTIVE;
    }

    /**
     * Helper method to activate the pass
     */
    public void activate() {
        this.status = PassStatus.ACTIVE;
    }

    /**
     * Helper method to deactivate the pass
     */
    public void deactivate() {
        this.status = PassStatus.INACTIVE;
    }
}



