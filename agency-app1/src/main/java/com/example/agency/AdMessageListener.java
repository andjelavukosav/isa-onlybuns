package com.example.agency;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AdMessageListener {

    @RabbitListener(queues = "#{anonymousQueue.name}")
    public void receiveMessage(AdPostMessage message) {
        System.out.println("🎯 NOVA REKLAMNA OBJAVA(RabbitMQ):");
        System.out.println("Opis: " + message.getDescription());
        System.out.println("Datum: " + message.getCreatedAt());
        System.out.println("Korisnik: " + message.getUsername());
        System.out.println("----------------------------------------");
    }
}
