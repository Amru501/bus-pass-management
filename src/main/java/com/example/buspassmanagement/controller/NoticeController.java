package com.example.buspassmanagement.controller;

import java.security.Principal;

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

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.model.Notice;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.BusService;
import com.example.buspassmanagement.service.NoticeService;
import com.example.buspassmanagement.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BusService busService;

    @GetMapping
    public String listNotices(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "notices";
    }

    @GetMapping("/add")
    // Since drivers can no longer log in, only ADMINs can post notices.
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String addNoticeForm(Model model) {
        if (!model.containsAttribute("notice")) {
            Notice notice = new Notice();
            notice.setBus(new Bus());
            model.addAttribute("notice", notice);
        }
        try {
            model.addAttribute("buses", busService.getAllBuses());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Could not load bus list. Please check system configuration.");
        }
        return "notices-add";
    }

    @PostMapping("/add")
    // Authorization updated to ADMIN only.
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String saveNotice(@Valid @ModelAttribute("notice") Notice notice,
                             BindingResult bindingResult,
                             @RequestParam("busId") String busIdValue,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {

        User poster = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found for principal: " + principal.getName()));

        if (busIdValue == null || busIdValue.isEmpty()) {
            bindingResult.rejectValue("bus", "bus.required", "Please select a target bus or 'ALL BUSES'.");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.notice", bindingResult);
            redirectAttributes.addFlashAttribute("notice", notice);
            return "redirect:/notices/add";
        }

        try {
            if ("ALL".equalsIgnoreCase(busIdValue)) {
                // Create a single notice with bus = null for "All Buses"
                Notice newNotice = new Notice();
                newNotice.setMessage(notice.getMessage());
                newNotice.setPostedBy(poster);
                newNotice.setBus(null); // null indicates "All Buses"
                noticeService.addNotice(newNotice);
                redirectAttributes.addFlashAttribute("successMessage", "Notice successfully posted to all buses.");
            } else {
                Long busId = Long.parseLong(busIdValue);
                Bus targetBus = busService.getBusById(busId);

                if (targetBus == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Error: Target bus not found.");
                    return "redirect:/notices/add";
                }

                notice.setBus(targetBus);
                notice.setPostedBy(poster);
                noticeService.addNotice(notice);
                redirectAttributes.addFlashAttribute("successMessage", "Notice successfully posted to bus: " + targetBus.getBusNumber() + ".");
            }
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid bus ID provided.");
            return "redirect:/notices/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while saving the notice.");
            return "redirect:/notices/add";
        }

        return "redirect:/notices";
    }

    @GetMapping("/delete/{id}")
    // Authorization updated to ADMIN only.
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        noticeService.deleteNotice(id);
        redirectAttributes.addFlashAttribute("successMessage", "Notice deleted successfully.");
        return "redirect:/notices";
    }
}
