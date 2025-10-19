package com.example.buspassmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank; // Import required
import jakarta.validation.constraints.NotNull; // Import required
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bus implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Bus number is required.") // ⬅️ ADDED
    @Column(nullable = false, unique = true)
    private String busNumber;

    @NotBlank(message = "Route is required.") // ⬅️ ADDED
    @Column(nullable = false)
    private String route;

    @NotNull(message = "Number of seats is required.") // ⬅️ ADDED
    @Min(value = 1, message = "Bus must have at least 1 seat.") // ⬅️ ADDED
    @Column(nullable = false) // ⬅️ ADDED for primitive int
    private int seats;

    @NotBlank(message = "Schedule is required.") // ⬅️ ADDED
    @Column(nullable = false) // ⬅️ ADDED
    private String schedule;
}