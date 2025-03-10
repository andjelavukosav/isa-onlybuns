package rs.ac.uns.ftn.informatika.jpa.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.AddressDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.*;
import rs.ac.uns.ftn.informatika.jpa.repository.AddressRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.GeocodingService;
import rs.ac.uns.ftn.informatika.jpa.service.RoleService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private UserDTOMapper userDTOMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GeocodingService geocodingService;

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

    @Transactional
    @Override
    public User save(UserDTO userRequest) {

        // Proverite da li korisničko ime već postoji uz zaključavanje
        entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username")
                .setParameter("username", userRequest.getUsername())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();

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

        // If address data exists in the UserDTO, set it
        if (userRequest.getAddress() != null) {
            AddressDTO addressDTO = userRequest.getAddress();
            Address address = new Address();;

            if (addressDTO.getId() > 0) {
                // Pokušaj pronalaska adrese u bazi
                address = addressRepository.findById(addressDTO.getId()).orElse(null);

                // Ako postoji, ažuriraj podatke
                if (address != null) {
                    address.setCountry(addressDTO.getCountry());
                    address.setCity(addressDTO.getCity());
                    address.setStreet(addressDTO.getStreet());
                    address.setStreetNumber(addressDTO.getStreetNumber());
                } else {
                    // Ako ne postoji, kreiraj novu
                    address = new Address();
                    address.setCountry(addressDTO.getCountry());
                    address.setCity(addressDTO.getCity());
                    address.setStreet(addressDTO.getStreet());
                    address.setStreetNumber(addressDTO.getStreetNumber());
                }
            } else {
                // Ako ID nije postavljen ili je 0, kreiraj novu adresu
                address = new Address();
                address.setCountry(addressDTO.getCountry());
                address.setCity(addressDTO.getCity());
                address.setStreet(addressDTO.getStreet());
                address.setStreetNumber(addressDTO.getStreetNumber());
            }
            String fullAddress = address.getStreet() + " " + address.getStreetNumber() + ", "
                    + address.getCity() + ", "
                    + address.getCountry();

            try {
                Location location = geocodingService.getCoordinates(fullAddress);
                address.setLocation(location);
            } catch (IOException e) {
                e.printStackTrace(); // Bolja obrada greške može uključivati logovanje
            }


            // Sačuvaj adresu u bazi (novu ili ažuriranu)
            address = addressRepository.save(address);

            // Postavi adresu korisniku
            u.setAddress(address);
        }


        // Save the user and return the saved entity
        return this.userRepository.save(u);
    }

    public User updateUser(int id, UserDTO userRequest) throws AccessDeniedException {
        // Find the user by ID
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Update user fields
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setPassword(userRequest.getPassword());
        existingUser.setFirstName(userRequest.getFirstname());
        existingUser.setLastName(userRequest.getLastname());
        existingUser.setEnabled(userRequest.isEnabled());
        existingUser.setEmail(userRequest.getEmail());

        // Update address if provided in the request
        if (userRequest.getAddress() != null) {
            AddressDTO addressDTO = userRequest.getAddress();
            Address address = null;

            if (addressDTO.getId() > 0) {
                // Ako ID postoji u request-u, pokušaj pronaći adresu u bazi
                address = addressRepository.findById(addressDTO.getId()).orElse(null);
            }

            if (address == null) {
                // Ako adresa nije pronađena (ili ID nije dat), koristi postojeću adresu korisnika
                address = existingUser.getAddress();
            }

            if (address == null) {
                // Ako korisnik nema adresu i nije pronađena u bazi, kreiraj novu
                address = new Address();
            }

            // Ažuriranje podataka o adresi
            address.setCountry(addressDTO.getCountry());
            address.setCity(addressDTO.getCity());
            address.setStreet(addressDTO.getStreet());
            address.setStreetNumber(addressDTO.getStreetNumber());

            String fullAddress = address.getStreet() + " " + address.getStreetNumber() + ", "
                    + address.getCity() + ", "
                    + address.getCountry();

            try {
                Location location = geocodingService.getCoordinates(fullAddress);
                address.setLocation(location);
            } catch (IOException e) {
                e.printStackTrace(); // Bolja obrada greške može uključivati logovanje
            }


            // Sačuvaj adresu u bazi
            address = addressRepository.save(address);

            // Postavi ažuriranu ili novu adresu korisniku
            existingUser.setAddress(address);
        }

        // Save the updated user and return the saved entity
        return userRepository.save(existingUser);
    }



    public List<Post> getAllPostsByUser(int userId) {
        return userRepository.findPostsByUserId(userId);
    }

    @Override
    public List<UserDTO> findUsersByRoleExcludingAdmin(int adminId) {
        List<User> users =  userRepository.findAllByRoleNameExcludingId("ROLE_USER", adminId);

        return users.stream().map(user -> new UserDTO(user)).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsers(String firstName, String lastName, String email, Long minPosts, Long maxPosts, int adminId, Sort sort) {
        return UserDTOMapper.toUserDTOList(userRepository.searchUserBy(firstName, lastName, email, minPosts, maxPosts, adminId, sort));
    }

    @Override
    public void updateUserPassword(int userId, String newPassword) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found."));

        // Hashujte novu lozinku
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        userRepository.save(user);
    }

    @Override
    public boolean verifyPassword(int userId, String currentPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional == null) {
            throw new RuntimeException("User not found.");
        }

        User user = userOptional.get();
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }


}