package rs.ac.uns.ftn.informatika.rabbitmq.model;

import java.io.Serializable;

public class Address implements Serializable {
    private static final long serialVersionUID = 1L;

    private String street;
    private String number;
    private String city;
    private String country;

    // Konstruktori
    public Address() {}

    public Address(String street, String number, String city, String country) {
        this.street = street;
        this.number = number;
        this.city = city;
        this.country = country;
    }

    // Getteri i setteri
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    @Override
    public String toString() {
        return street + " " + number + ", " + city + ", " + country;
    }

}
