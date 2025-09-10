package rs.ac.uns.ftn.informatika.jpa.dto;

public class AddressDTO {
    private String country;
    private String city;
    private String street;
    private String streetNumber;
    private int Id;
    public LocationDTO location;

    // Getters and setters


    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }
}
