package com.example.buspassmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.buspassmanagement.model.Faq;
import com.example.buspassmanagement.service.FaqService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/faq")
public class FaqController {

    @Autowired
    private FaqService faqService;

    @GetMapping
    public String showFaqs(Model model) {
        List<Faq> faqs = faqService.getAllFaqs();
        model.addAttribute("faqs", faqs);
        return "faq";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String manageFaqs(Model model) {
        List<Faq> faqs = faqService.getAllFaqs();
        model.addAttribute("faqs", faqs);
        model.addAttribute("newFaq", new Faq());
        return "faq-manage";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String addFaq(@Valid @ModelAttribute("newFaq") Faq faq,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fill in all required fields.");
            return "redirect:/faq/manage";
        }

        try {
            faqService.saveFaq(faq);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding FAQ: " + e.getMessage());
        }
        
        return "redirect:/faq/manage";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String editFaq(@PathVariable Long id,
                         @RequestParam("question") String question,
                         @RequestParam("answer") String answer,
                         @RequestParam("displayOrder") Integer displayOrder,
                         RedirectAttributes redirectAttributes) {
        try {
            Faq faq = new Faq();
            faq.setQuestion(question);
            faq.setAnswer(answer);
            faq.setDisplayOrder(displayOrder);
            
            faqService.updateFaq(id, faq);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating FAQ: " + e.getMessage());
        }
        
        return "redirect:/faq/manage";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deleteFaq(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            faqService.deleteFaq(id);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting FAQ: " + e.getMessage());
        }
        
        return "redirect:/faq/manage";
    }
}