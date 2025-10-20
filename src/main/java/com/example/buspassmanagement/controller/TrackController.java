package com.example.buspassmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrackController {

    /**
     * Displays the bus tracking page.
     * This is a placeholder for a future real-time tracking feature.
     */
    @GetMapping("/track")
    public String showTrackPage() {
        return "track"; // Returns track.html
    }
}
