package com.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Async
    public void sendOrderConfirmationEmail(String toEmail, String customerName, String orderId, Double totalAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("totalAmount", totalAmount);

            String htmlContent = templateEngine.process("order-confirmation", context);

            helper.setTo(toEmail);
            helper.setSubject("Order Confirmation - Farmer to Consumer Platform");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendNewOrderNotificationToFarmer(String toEmail, String farmerName, String orderId, String productName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("farmerName", farmerName);
            context.setVariable("orderId", orderId);
            context.setVariable("productName", productName);

            String htmlContent = templateEngine.process("new-order-notification", context);

            helper.setTo(toEmail);
            helper.setSubject("New Order Received - Farmer to Consumer Platform");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}