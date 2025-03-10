package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.service.GeocodingService;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class GeocodingServiceImpl implements GeocodingService {
    private static final String GEOCODING_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";

    @Override
    public Location getCoordinates(String address) throws IOException {
        String url = GEOCODING_URL + URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String response = reader.lines().collect(Collectors.joining());
            return parseCoordinates(response);
        }
    }


    private Location parseCoordinates(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode firstResult = rootNode.get(0);
                double latitude = firstResult.path("lat").asDouble();
                double longitude = firstResult.path("lon").asDouble();
                return new Location(latitude, longitude);
            }

            return null;
        } catch (IOException e) {
            throw new RuntimeException("Error parsing geocoding response", e);
        }
    }
}
