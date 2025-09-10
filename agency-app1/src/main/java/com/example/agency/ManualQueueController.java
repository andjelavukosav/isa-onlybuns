package com.example.agency;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ads")
public class ManualQueueController {

    @PostMapping("/receive")
    public void receiveAd(@RequestBody AdPostMessage ad) {
        System.out.println("🎯 NOVA REKLAMNA OBJAVA(Manual MQ):");
        System.out.println("Opis: " + ad.getDescription());
        System.out.println("Datum: " + ad.getCreatedAt());
        System.out.println("Korisnik: " + ad.getUsername());
        System.out.println("----------------------------------------");

    }
}
