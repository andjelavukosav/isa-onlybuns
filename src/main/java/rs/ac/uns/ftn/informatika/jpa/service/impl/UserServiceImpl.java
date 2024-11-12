package rs.ac.uns.ftn.informatika.jpa.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.AddressDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Address;
import rs.ac.uns.ftn.informatika.jpa.model.Role;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.AddressRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.RoleService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;
    @Autowired
    private AddressRepository addressRepository;

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(int id) throws AccessDeniedException {
        return userRepository.findById(id).orElseGet(null);
    }

    public List<User> findAll() throws AccessDeniedException {
        return userRepository.findAll();
    }

    @Override
    public User save(UserDTO userRequest) {
        // Create a new User entity
        User u = new User();
        u.setUsername(userRequest.getUsername());

        // Encrypt password
        u.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // Set user details
        u.setFirstName(userRequest.getFirstname());
        u.setLastName(userRequest.getLastname());
        u.setEnabled(userRequest.isEnabled());
        u.setEmail(userRequest.getEmail());

        // Get the roles and assign to user
        List<Role> roles = roleService.findByName("ROLE_USER");
        u.setRoles(roles);

        // Handle address
        Address address = new Address();

        // If address data exists in the UserDTO, set it
        if (userRequest.getAddress() != null) {
            AddressDTO addressDTO = userRequest.getAddress();

            // Check if the address already exists in the database (based on some unique criteria like country, city, etc.)
            address = addressRepository.findByCountryAndCityAndStreetAndStreetNumber(
                    addressDTO.getCountry(),
                    addressDTO.getCity(),
                    addressDTO.getStreet(),
                    addressDTO.getStreetNumber()
            );

            // If address doesn't exist, create a new one
            if (address == null) {
                address = new Address();
                address.setCountry(addressDTO.getCountry());
                address.setCity(addressDTO.getCity());
                address.setStreet(addressDTO.getStreet());
                address.setStreetNumber(addressDTO.getStreetNumber());
                addressRepository.save(address); // Save new address
            }

            // Set the address for the user
            u.setAddress(address);
        }

        // Save the user and return the saved entity
        return this.userRepository.save(u);
    }

    public User updateUser(int id, UserDTO userRequest) throws AccessDeniedException {
        // Find the user by ID
        User existingUser = userRepository.findById(id).orElseThrow(() -> new AccessDeniedException("User not found"));

        // Update the user fields
        existingUser.setUsername(userRequest.getUsername());



        // Update user details
        existingUser.setPassword(userRequest.getPassword());
        existingUser.setFirstName(userRequest.getFirstname());
        existingUser.setLastName(userRequest.getLastname());
        existingUser.setEnabled(userRequest.isEnabled());
        existingUser.setEmail(userRequest.getEmail());

        // Update roles if necessary
        // If roles need to be updated, we can add logic to handle that. For simplicity, we retain the same role.
        // List<Role> roles = roleService.findByName("ROLE_USER");
        // existingUser.setRoles(roles);

        // Handle address
        if (userRequest.getAddress() != null) {
            AddressDTO addressDTO = userRequest.getAddress();
            Address address = addressRepository.findByCountryAndCityAndStreetAndStreetNumber(
                    addressDTO.getCountry(),
                    addressDTO.getCity(),
                    addressDTO.getStreet(),
                    addressDTO.getStreetNumber()
            );

            // If address doesn't exist, create a new one
            if (address == null) {
                address = new Address();
                address.setCountry(addressDTO.getCountry());
                address.setCity(addressDTO.getCity());
                address.setStreet(addressDTO.getStreet());
                address.setStreetNumber(addressDTO.getStreetNumber());
                addressRepository.save(address); // Save new address
            }

            // Update the user's address
            existingUser.setAddress(address);
        }

        // Save the updated user and return the saved entity
        return userRepository.save(existingUser);
    }



    @Override
    public List<UserDTO> findUsersByRoleExcludingAdmin(int adminId) {
        List<User> users =  userRepository.findAllByRoleName("ROLE_USER")
                .stream()
                .filter(user -> user.getId() != adminId) // isključuje administratora
                .collect(Collectors.toList());

        return users.stream().map(user -> new UserDTO(user)).collect(Collectors.toList());
    }

    @Override
    public List<User> searchUsers(String firstName, String lastName, String email) {
        return this.userRepository.filterUsers(firstName, lastName, email);
    }

}