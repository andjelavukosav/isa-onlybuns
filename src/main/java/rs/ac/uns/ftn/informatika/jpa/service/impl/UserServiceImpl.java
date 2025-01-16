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
import rs.ac.uns.ftn.informatika.jpa.model.Address;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.Role;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.AddressRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.RoleService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;
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

    @Autowired
    private UserDTOMapper userDTOMapper;

    @Autowired
    private EntityManager entityManager;

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
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Update the user fields
        existingUser.setUsername(userRequest.getUsername());

        // Update user details
        existingUser.setPassword(userRequest.getPassword());
        existingUser.setFirstName(userRequest.getFirstname());
        existingUser.setLastName(userRequest.getLastname());
        existingUser.setEnabled(userRequest.isEnabled());
        existingUser.setEmail(userRequest.getEmail());


        // Update address if provided in the request
        if (existingUser.getAddress() != null) {

            //adresa korisnika koji se treba updatovat
            Address address = addressRepository.findByCountryAndCityAndStreetAndStreetNumber(
                    existingUser.getAddress().getCountry(),
                    existingUser.getAddress().getCity(),
                    existingUser.getAddress().getStreet(),
                    existingUser.getAddress().getStreetNumber()
            );
            // If the address exists, update it with the new details (optional if you want to allow changes)
            address.setCountry(userRequest.getAddress().getCountry());
            address.setCity(userRequest.getAddress().getCity());
            address.setStreet(userRequest.getAddress().getStreet());
            address.setStreetNumber(userRequest.getAddress().getStreetNumber());
            addressRepository.save(address); // Save the updated address

            // Set the user's address to the existing or newly created address
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


}