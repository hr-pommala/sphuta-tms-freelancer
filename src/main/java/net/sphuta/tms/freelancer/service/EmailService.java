package net.sphuta.tms.freelancer.service;


import org.springframework.stereotype.Service;
@Service
public class EmailService {
    public void send(String to, String subject, String body) {
        // dev: log to console. Replace with actual email provider in production.
        System.out.println("=== Sending Email ===\nTo: "+to+"\nSubject: "+subject+"\nBody: "+body);
    }
}
