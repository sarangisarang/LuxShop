package com.luxshop.shop.service;

import com.luxshop.shop.domain.Orders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends order confirmation emails. If SMTP is configured (spring.mail.host), a
 * JavaMailSender bean exists and the message is sent; otherwise it is logged, so
 * checkout works everywhere and the flow is testable without a mail server.
 * Failures never propagate — a checkout must not fail because email did.
 */
@Service
public class OrderEmailService {

    private static final Logger log = LoggerFactory.getLogger(OrderEmailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public OrderEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                             @Value("${luxshop.mail.from:noreply@luxshop.example}") String from) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.from = from;
    }

    public void sendOrderConfirmation(Orders order) {
        if (order.getCustomer() == null || order.getCustomer().getEmail() == null) {
            return;
        }
        String to = order.getCustomer().getEmail();
        String subject = "LuxShop — order #" + order.getOrderNo() + " confirmed";
        String body = buildBody(order);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Order confirmation emailed to {}", to);
            } catch (Exception e) {
                log.warn("Could not email order #{} to {}: {}", order.getOrderNo(), to, e.getMessage());
            }
        } else {
            log.info("[order-email: no SMTP configured, logging only]\nTo: {}\nSubject: {}\n{}", to, subject, body);
        }
    }

    private String buildBody(Orders order) {
        String name = order.getCustomer().getFirstName() != null
                ? order.getCustomer().getFirstName() : "there";
        return "Hi " + name + ",\n\n"
                + "Thank you for your order! We've received order #" + order.getOrderNo()
                + " placed on " + order.getOrderDate() + ".\n"
                + "Total: " + order.getOrderTotal() + " GEL.\n"
                + "Current status: " + order.getOrderStatus() + ".\n\n"
                + "We'll let you know when it ships.\n\n— The LuxShop team";
    }
}
