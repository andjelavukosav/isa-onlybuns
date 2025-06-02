package rs.ac.uns.ftn.informatika.jpa.webSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import rs.ac.uns.ftn.informatika.jpa.model.AsylumAndVeterinarian;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.service.AsylumAndVeterinarianService;
import rs.ac.uns.ftn.informatika.jpa.service.GeocodingService;

import java.io.IOException;

@Component
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LocationWebSocketHandler.class);

    private final AsylumAndVeterinarianService asylumAndVeterinarianService;
    private final GeocodingService geocodingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocationWebSocketHandler(AsylumAndVeterinarianService asylumAndVeterinarianService,
                                    GeocodingService geocodingService) {
        this.asylumAndVeterinarianService = asylumAndVeterinarianService;
        this.geocodingService = geocodingService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        log.info("WebSocket received message: " + payload);

        try {
            // Parsiraj JSON u objekat
            AsylumAndVeterinarian data = objectMapper.readValue(payload, AsylumAndVeterinarian.class);

            // Dobij koordinate preko geokodiranja
            String fullAddress = data.getAddress().getStreet() + " " + data.getAddress().getNumber() + ", " +
                    data.getAddress().getCity() + ", " + data.getAddress().getCountry();

            Location location = geocodingService.getCoordinates(fullAddress);
            data.getAddress().setLocation(location);

            // Sačuvaj objekat u bazi
            asylumAndVeterinarianService.save(data);

            log.info("Poruka uspešno obrađena i sačuvana.");
        } catch (Exception e) {
            log.error("Greška pri obradi poruke sa WebSocket-a", e);
            session.sendMessage(new TextMessage("Error processing message: " + e.getMessage()));
        }
    }
}
