package com.example.buspassmanagement.model;

import java.io.Serializable;

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

    @NotBlank(message = "Bus number is required.")
    @Column(nullable = false, unique = true)
    private String busNumber;

    @NotBlank(message = "Route is required.")
    @Column(nullable = false)
    private String route;

    @NotNull(message = "Number of seats is required.")
    @Min(value = 1, message = "Bus must have at least 1 seat.")
    @Column(nullable = false)
    private int seats;

    @NotBlank(message = "Schedule is required.")
    @Column(nullable = false)
    private String schedule;
}
