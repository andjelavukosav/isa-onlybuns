package rs.ac.uns.ftn.informatika.jpa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "asylum_and_veterinarian")
public class AsylumAndVeterinarian implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "name")
    private String name;


    @Embedded
    private AddressAsylum address;  // A user can have one address

    public AsylumAndVeterinarian() {
    }


    public AsylumAndVeterinarian(String identifier, String name, AddressAsylum address) {
        this.identifier = identifier;
        this.name = name;
        this.address = address;
    }

    // Getteri i setteri

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AddressAsylum getAddress() { return address; }
    public void setAddress(AddressAsylum address) { this.address = address; }

    @Override
    public String toString() {
        return "AsylumAndVeterinarian{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", address=" + address +
                '}';
    }
}
