package rs.ac.uns.ftn.informatika.rabbitmq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsylumAndVeterinarian implements Serializable {
    private static final long serialVersionUID = 1L;

    private String identifier;
    private String name;
    private Address address;  // Sada koristimo klasu Address

    public AsylumAndVeterinarian() {
    }


    public AsylumAndVeterinarian(String identifier, String name, Address address) {
        this.identifier = identifier;
        this.name = name;
        this.address = address;
    }

    // Getteri i setteri
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    @Override
    public String toString() {
        return "AsylumAndVeterinarian{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", address=" + address +
                '}';
    }
}
