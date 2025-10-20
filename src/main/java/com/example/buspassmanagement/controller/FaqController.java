package com.example.buspassmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaqController {

    /**
     * Displays the Frequently Asked Questions page.
     * @return the name of the FAQ view template.
     */
    @GetMapping("/faq")
    public String showFaqPage() {
        return "faq"; // Renders faq.html
    }
}
