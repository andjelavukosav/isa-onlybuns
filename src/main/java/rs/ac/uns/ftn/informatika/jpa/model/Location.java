package rs.ac.uns.ftn.informatika.jpa.model;

import rs.ac.uns.ftn.informatika.jpa.dto.LocationDTO;

import javax.persistence.Embeddable;

@Embeddable
public class Location {
    private double latitude;
    private double longitude;

    public Location() {}

    public Location(LocationDTO locationDTO) {
        this.latitude = locationDTO.latitude;
        this.longitude = locationDTO.longitude;
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
