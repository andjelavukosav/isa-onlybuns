package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.model.Location;
import java.io.IOException;

public interface GeocodingService {
    // Metod za dobijanje koordinata na osnovu adrese
    Location getCoordinates(String address) throws IOException;
}
