package rs.ac.uns.ftn.informatika.jpa.dto;

import rs.ac.uns.ftn.informatika.jpa.model.Location;

public class LocationDTO {
    public double latitude;
    public double longitude;

    public LocationDTO() {}

    public LocationDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationDTO(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }
}
