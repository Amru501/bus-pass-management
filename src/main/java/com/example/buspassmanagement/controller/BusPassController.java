package com.example.buspassmanagement.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.buspassmanagement.model.BusPass;
import com.example.buspassmanagement.model.User;
import com.example.buspassmanagement.service.BusPassService;
import com.example.buspassmanagement.service.PaymentService;
import com.example.buspassmanagement.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Controller
public class BusPassController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BusPassService busPassService;

    @GetMapping("/pass")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String showBusPass(Model model, Principal principal) {
        try {
            // 1. Get the currently logged-in user
            User currentUser = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Get user's bus pass
            BusPass busPass = busPassService.findByUser(currentUser).orElse(null);
            
            // 3. Determine the pass status
            String passStatus = (busPass != null && busPass.isActive()) 
                ? "ACTIVE" : "INACTIVE";

            // 4. Get payment status for additional info
            PaymentService.PaymentStatusInfo paymentStatus = paymentService.getPaymentStatus(currentUser);

            // 5. Generate QR Code with user's info
            String selectedRoute = (busPass != null && busPass.getSelectedRoute() != null) 
                ? busPass.getSelectedRoute() : "Not Selected";
            
            String qrCodeText = "Name: " + currentUser.getName() + 
                              "\nEmail: " + currentUser.getEmail() + 
                              "\nRoute: " + selectedRoute +
                              "\nStatus: " + passStatus;
            byte[] qrCodeImageBytes = generateQRCodeImage(qrCodeText, 250, 250);
            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeImageBytes);

            // 6. Add data to the model for the template
            model.addAttribute("user", currentUser);
            model.addAttribute("busPass", busPass);
            model.addAttribute("passStatus", passStatus);
            model.addAttribute("qrCodeImage", qrCodeBase64);
            model.addAttribute("paymentStatus", paymentStatus);

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
