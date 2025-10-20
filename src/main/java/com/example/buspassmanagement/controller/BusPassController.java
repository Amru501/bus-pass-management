package com.example.buspassmanagement.controller;

import com.example.buspassmanagement.model.Payment;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.PaymentService;
import com.example.buspassmanagement.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

@Controller
public class BusPassController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/pass")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String showBusPass(Model model, Principal principal) {
        try {
            // 1. Get the currently logged-in user
            User currentUser = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Determine the pass status based on payments
            List<Payment> payments = paymentService.getPaymentsByUserId(currentUser.getId());
            boolean hasPendingPayments = payments.stream()
                    .anyMatch(p -> p.getStatus() == Payment.PaymentStatus.PENDING);
            String passStatus = hasPendingPayments ? "INACTIVE" : "ACTIVE";

            // 3. Generate QR Code with user's info
            String qrCodeText = "Name: " + currentUser.getName() + "\nEmail: " + currentUser.getEmail() + "\nStatus: " + passStatus;
            byte[] qrCodeImageBytes = generateQRCodeImage(qrCodeText, 250, 250);
            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeImageBytes);

            // 4. Add data to the model for the template
            model.addAttribute("user", currentUser);
            model.addAttribute("passStatus", passStatus);
            model.addAttribute("qrCodeImage", qrCodeBase64);

            return "bus-pass"; // Renders bus-pass.html

        } catch (Exception e) {
            e.printStackTrace();
            // Redirect to an error page or show an error message
            return "redirect:/error";
        }
    }

    private byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", outputStream);

        return outputStream.toByteArray();
    }
}
