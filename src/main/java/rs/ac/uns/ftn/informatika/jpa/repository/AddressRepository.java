package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.jpa.model.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    Address findByCountryAndCityAndStreetAndStreetNumber(String country, String city, String street, String streetNumber);

}
