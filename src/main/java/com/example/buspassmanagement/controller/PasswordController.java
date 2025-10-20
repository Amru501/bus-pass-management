package com.example.buspassmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.NotificationService;
import com.example.buspassmanagement.service.OtpService;
import com.example.buspassmanagement.service.UserService;

import jakarta.validation.constraints.Email;

@Controller
public class PasswordController {

	@Autowired
	private UserService userService;

	@Autowired
	private OtpService otpService;

	@Autowired
	private NotificationService notificationService;

	@GetMapping("/forgot-password")
	public String showForgotPassword(Model model) {
		return "forgot-password";
	}

	@PostMapping("/forgot-password")
	public String handleForgotPassword(@Email @RequestParam("email") String email,
					 RedirectAttributes redirectAttributes) {
		var userOpt = userService.findByEmail(email);
		if (userOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "No account found with that email.");
			return "redirect:/forgot-password";
		}
		User user = userOpt.get();
		String otp = otpService.generateOtpFor(email);
		notificationService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp + " (valid for 5 minutes)");
		notificationService.sendSms(user.getPhone(), "Your OTP is: " + otp + " (valid 5m)");
		redirectAttributes.addFlashAttribute("successMessage", "An OTP has been sent to your email and phone.");
		redirectAttributes.addFlashAttribute("email", email);
		return "redirect:/forgot-password/verify";
	}

	@GetMapping("/forgot-password/verify")
	public String showVerify(Model model) {
		return "verify-otp";
	}

	@PostMapping("/forgot-password/verify")
	public String handleVerify(@RequestParam("email") String email,
					 @RequestParam("otp") String otp,
					 RedirectAttributes redirectAttributes) {
		if (!otpService.verify(email, otp)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Invalid or expired OTP.");
			redirectAttributes.addFlashAttribute("email", email);
			return "redirect:/forgot-password/verify";
		}
		redirectAttributes.addFlashAttribute("email", email);
		return "redirect:/forgot-password/reset";
	}

	@GetMapping("/forgot-password/reset")
	public String showReset(Model model) {
		return "reset-password";
	}

	@PostMapping("/forgot-password/reset")
	public String handleReset(@RequestParam("email") String email,
					 @RequestParam("password") String password,
					 RedirectAttributes redirectAttributes) {
		var userOpt = userService.findByEmail(email);
		if (userOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Restart reset flow.");
			return "redirect:/forgot-password";
		}
		User user = userOpt.get();
		user.setPassword(password);
		userService.saveUser(user);
		otpService.clear(email);
		redirectAttributes.addFlashAttribute("successMessage", "Password updated. Please log in.");
		return "redirect:/login";
	}
}
