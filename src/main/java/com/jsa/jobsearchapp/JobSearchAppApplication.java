package com.jsa.jobsearchapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class JobSearchAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobSearchAppApplication.class, args);
    }

//    @Bean
//    CommandLineRunner testEmail(JavaMailSender mailSender) {
//        return args -> {
//            System.out.println("--- Attempting to send a test email to Onet... ---");
//
//            try {
//                SimpleMailMessage message = new SimpleMailMessage();
//                message.setFrom("noreply.jobsearch@op.pl"); // Must match your username!
//                message.setTo("daniel.gawin.2003@gmail.com"); // Send to yourself
//                message.setSubject("Spring Boot SMTP Test");
//                message.setText("If you see this, your Onet SMTP config is correct!");
//
//                mailSender.send(message);
//                System.out.println("--- Success! Check your inbox. ---");
//            } catch (Exception e) {
//                System.err.println("--- Failed! Error: " + e.getMessage());
//                // This will print the stack trace to help us debug
//                e.printStackTrace();
//            }
//        };
//    }
}
