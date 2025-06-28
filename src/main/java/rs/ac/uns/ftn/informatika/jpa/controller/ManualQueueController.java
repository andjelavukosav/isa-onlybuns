package rs.ac.uns.ftn.informatika.jpa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.model.AsylumAndVeterinarian;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.service.AsylumAndVeterinarianService;
import rs.ac.uns.ftn.informatika.jpa.service.GeocodingService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ManualQueueController {

    private static final Logger logger = LoggerFactory.getLogger(ManualQueueController.class);

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private AsylumAndVeterinarianService asylumAndVeterinarianService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 📥 Prijem i obrada poruke odmah po prijemu (bez reda, bez polling-a)
    @PostMapping("/queue/manual")
    public void receiveAndProcess(@RequestBody AsylumAndVeterinarian message) {
        logger.info("📥 Primljena poruka bez MQ: {}", message);

        String fullAddress = message.getAddress().getStreet() + " " + message.getAddress().getNumber() + ", "
                + message.getAddress().getCity() + ", " + message.getAddress().getCountry();

        try {
            Location location = geocodingService.getCoordinates(fullAddress);
            if (location == null) {
                logger.error("❌ Geocoding nije uspeo za adresu: {}", fullAddress);
                return;
            }

            message.getAddress().setLocation(location);
            asylumAndVeterinarianService.save(message);

            // 🔄 Obaveštavanje Angular aplikacije
            messagingTemplate.convertAndSend("/topic/new-asylum", message);

            logger.info("✅ Poruka uspešno sačuvana i prosleđena na front.");
        } catch (IOException e) {
            logger.error("❌ Greška prilikom geokodiranja: {}", e.getMessage());
        }
    }
}

