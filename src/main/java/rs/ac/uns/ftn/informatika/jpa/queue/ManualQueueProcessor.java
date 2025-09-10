package rs.ac.uns.ftn.informatika.jpa.queue;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.jpa.model.AsylumAndVeterinarian;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.service.AsylumAndVeterinarianService;
import rs.ac.uns.ftn.informatika.jpa.service.GeocodingService;

import java.io.IOException;

@Component
public class ManualQueueProcessor {

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private AsylumAndVeterinarianService asylumAndVeterinarianService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    @Async
    public void processQueue() {
        new Thread(() -> {
            while (true) {
                try {
                    // 💤 Čekaj dok se ne pojavi poruka
                    AsylumAndVeterinarian message = ManualMessageQueue.takeMessage();

                    String fullAddress = message.getAddress().getStreet() + " " + message.getAddress().getNumber() + ", "
                            + message.getAddress().getCity() + ", " + message.getAddress().getCountry();

                    Location location = geocodingService.getCoordinates(fullAddress);
                    if (location == null) {
                        System.err.println("Geocoding failed for: " + fullAddress);
                        continue;
                    }

                    message.getAddress().setLocation(location);
                    asylumAndVeterinarianService.save(message);

                    messagingTemplate.convertAndSend("/topic/new-asylum", message);
                    System.out.println("✅ Poruka obrađena i prosleđena frontendu: " + message);

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
