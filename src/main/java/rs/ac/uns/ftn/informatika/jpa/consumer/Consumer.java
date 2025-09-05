package rs.ac.uns.ftn.informatika.jpa.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatMessageDTO;
import rs.ac.uns.ftn.informatika.jpa.model.AsylumAndVeterinarian;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.service.AsylumAndVeterinarianService;
import rs.ac.uns.ftn.informatika.jpa.service.GeocodingService;
import rs.ac.uns.ftn.informatika.jpa.service.impl.GeocodingServiceImpl;

import java.io.IOException;

@Component
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @Autowired
    private AsylumAndVeterinarianService asylumAndVeterinarianService;

    @Autowired
    private GeocodingService geocodingService;

    @RabbitListener(queues = "${myqueue}")
    public void handler(@Payload AsylumAndVeterinarian message) {
        log.info("Consumer> " + message);

        String fullAddress = message.getAddress().getStreet() + " " + message.getAddress().getNumber() + ", "
                + message.getAddress().getCity() + ", "
                + message.getAddress().getCountry();

        try {
            Location location = geocodingService.getCoordinates(fullAddress);
            if (location == null) {
                log.error("Geocoding failed: location is null for address: " + fullAddress);
                return; //  Ne pokušavaj da sačuvaš ako nema lokacije
            }
            message.getAddress().setLocation(location);
        } catch (IOException e) {
            log.error("Error while getting coordinates for address: " + fullAddress, e);
            return; //  Ne pokušavaj da sačuvaš ako je došlo do greške
        }

        // Lokacija postoji — možeš sačuvati
        this.asylumAndVeterinarianService.save(message);

        // 📤 Pošalji poruku ka Angular aplikaciji
        messagingTemplate.convertAndSend("/topic/new-asylum", message);
    }


}
