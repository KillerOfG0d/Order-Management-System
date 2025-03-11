package com.ordermanager.control;

import com.ordermanager.entity.Order;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderCompletionEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUser().getEmail());
            message.setSubject("Order Completed: " + order.getId());
            message.setText("Dear " + order.getUser().getName() + ",\n\n" +
                    "Your order #" + order.getId() + " for " + order.getQuantity() + " units of " +
                    order.getItem().getName() + " has been completed.\n\n" +
                    "Thank you for your business!\n\n" +
                    "Order Management System");

            mailSender.send(message);
            logger.info("Email sent for completed order: {}", order.getId());
        } catch (Exception e) {
            logger.error("Failed to send email for order: {}", order.getId(), e);
        }
    }
}

