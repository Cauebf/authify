package com.github.cauebf.authfy.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // thymeleaf template engine used to process html email templates
    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {
        // creates a simple text-only email message
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Our Platform!");
        message.setText("Hello " + name + ",\n\n"
                + "Thank you for registering at our platform. We're excited to have you on board!\n\n"
                + "Best regards,\n"
                + "Authify Team");
                
        mailSender.send(message);
    }

//    public void sendResetOtpEmail(String toEmail, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("Password Reset OTP");
//        message.setText("Your OTP for password reset is: " + otp + "\n\n"
//                + "This OTP will expire in 15 minutes.");
//
//        mailSender.send(message);
//    }

    public void sendResetOtpEmail(String toEmail, String otp) throws MessagingException {
        // creates thymeleaf context to pass dynamic variables to the html template
        Context context = new Context();
        context.setVariable("email", toEmail);
        context.setVariable("otp", otp);

        // processes the "password-reset-email.html" file with the provided data
        String process = templateEngine.process("password-reset-email", context);

        // creates a mime message to allow sending html content to the email
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // helper class to simplify mime message configuration
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        // sets the email properties
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Password Reset OTP");
        helper.setText(process, true); // sets the processed html as the email body

        mailSender.send(mimeMessage);
    }

//    public void sendVerifyOtpEmail(String toEmail, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("Account Verification OTP");
//        message.setText("Your OTP is: " + otp + "\n\n"
//                + "This OTP will expire in 24 hours.");
//
//        mailSender.send(message);
//    }

    public void sendVerifyOtpEmail(String toEmail, String otp) throws MessagingException {
        // creates thymeleaf context to pass dynamic variables to the html template
        Context context = new Context();
        context.setVariable("email", toEmail);
        context.setVariable("otp", otp);

        // processes the "verify-email.html" file with the provided data
        String process = templateEngine.process("verify-email", context);

        // creates a mime message to allow sending html content to the email
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // helper class to simplify mime message configuration
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        // sets the email properties
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Account Verification OTP");
        helper.setText(process, true); // sets the processed html as the email body

        mailSender.send(mimeMessage);
    }
}
