package rs.ac.uns.ftn.informatika.jpa.service.impl;

import com.google.common.hash.BloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.*;
import rs.ac.uns.ftn.informatika.jpa.exception.DuplicateResourceException;
import rs.ac.uns.ftn.informatika.jpa.mapper.PostDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.*;
import rs.ac.uns.ftn.informatika.jpa.model.*;
import rs.ac.uns.ftn.informatika.jpa.pagedResults.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.repository.AddressRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.*;
import rs.ac.uns.ftn.informatika.jpa.specification.UserSpecification;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.io.IOException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private BloomFilter<String> usernameBloomFilter;

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
    private PostDTOMapper postDTOMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private LikeService likeService;

    @PersistenceContext
    private EntityManager entityManagerr;

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
        try {

            Optional<User> existingUserByEmail = userRepository.findUserByEmailOptional(userRequest.getEmail());
            if (existingUserByEmail.isPresent()) {
                throw new DuplicateResourceException("Email address already exists.");
            }

            if (usernameBloomFilter.mightContain(userRequest.getUsername())) {
                Optional<User> existingUserByUsername = userRepository.findByUsernameOptional(userRequest.getUsername());
                if (existingUserByUsername.isPresent()) {
                    throw new DuplicateResourceException("Username already exists.");
                }
            }

            // Create new user entity
            User u = new User();
            u.setUsername(userRequest.getUsername());
            u.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Encrypt password
            u.setFirstName(userRequest.getFirstname());
            u.setLastName(userRequest.getLastname());
            u.setEnabled(userRequest.isEnabled());
            u.setEmail(userRequest.getEmail());
            u.setLastPasswordResetDate(userRequest.getLastPasswordResetDate());

            // Assign default role
            List<Role> roles = roleService.findByName("ROLE_USER");
            u.setRoles(roles);

            // Handle address if present
            if (userRequest.getAddress() != null) {
                AddressDTO addressDTO = userRequest.getAddress();
                Address address;

                if (addressDTO.getId() > 0) {
                    // Try to find address in database
                    address = addressRepository.findById(addressDTO.getId()).orElse(null);

                    if (address != null) {
                        // Update existing address
                        address.setCountry(addressDTO.getCountry());
                        address.setCity(addressDTO.getCity());
                        address.setStreet(addressDTO.getStreet());
                        address.setStreetNumber(addressDTO.getStreetNumber());
                    } else {
                        // Create new address
                        address = new Address();
                        address.setCountry(addressDTO.getCountry());
                        address.setCity(addressDTO.getCity());
                        address.setStreet(addressDTO.getStreet());
                        address.setStreetNumber(addressDTO.getStreetNumber());
                    }
                } else {
                    // ID not set, create new address
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
                    System.err.println("Error during address geocoding: " + fullAddress + ". Details: " + e.getMessage());
                }

                // Save address in database
                address = addressRepository.save(address);

                // Assign address to user
                u.setAddress(address);
            }

            // Attempt to save the user — if uniqueness constraint fails, it will throw an exception
            User savedUser = this.userRepository.save(u);

            // Add the username to the Bloom filter
            usernameBloomFilter.put(savedUser.getUsername());

            return savedUser;

        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

            if (errorMessage.contains("uk_user_email") || errorMessage.contains("email")) {
                throw new DuplicateResourceException("Email address already exists.", e);
            } else if (errorMessage.contains("uk_user_username") || errorMessage.contains("username")) {
                throw new DuplicateResourceException("Username already exists.", e);
            }

            throw e; // Re-throw other unhandled integrity issues
        }

    }


    public User updateUser(int id, UserDTO userRequest) throws AccessDeniedException {
        // Find the user by ID
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Update user fields
        existingUser.setUsername(userRequest.getUsername());



        // Update user details
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
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersExcludingAdmin(int adminId, Pageable pageable) {

        Page<User> userPage =  userRepository.getUsersByRoleNameExcludingId("ROLE_USER", adminId, pageable);

        List<UserDTO> usersOnCurrentPage = userDTOMapper.toUserDTOList(userPage.getContent());

        return new PageImpl<>(usersOnCurrentPage, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(UserSearchCriteria criteria, Pageable pageable, int adminId) {

        if(criteria.isEmpty()){
            System.out.println("CRITERIA IS EMPTY ");
            return getUsersExcludingAdmin(adminId, pageable);
        }

        Specification<User> userSpecification = new UserSpecification(criteria);

        Specification<User> adminExclusion = (root, query, builder) -> builder.notEqual(root.get("id"), adminId);

        Specification<User> concludedSpecification = Specification.where(userSpecification).and(adminExclusion);

        Page<User> usersPage = userRepository.findAll(concludedSpecification, pageable);

        List<UserDTO> usersOnPage = userDTOMapper.toUserDTOList(usersPage.getContent());

        return new PageImpl<>(usersOnPage, pageable, usersPage.getTotalElements());
    }

    @Override
    public List<UserDTO> findAllContainingUsername(String username){
        return userDTOMapper.toUserDTOList(userRepository.findByUsernameContainingIgnoreCase(username));
    }

    @Override
    @Transactional
    public PagedResults<PostDTO> getFollowingPosts(int userId) {
        List<User> followings = getFollowing(userId);

        if(followings.isEmpty()){
            return new PagedResults<>(Collections.emptyList(), 0);
        }

        List<PostDTO> posts = followings.stream()
                .flatMap(followed -> followed.getPosts().stream())
                .sorted(Comparator.comparing(Post::getCreationDateTime).reversed())
                .map(post -> {
                    boolean isLiked = likeService.findLikeByPostIdAndUserId(post.getId(), userId) != null ;

                    PostDTO postDTO = new PostDTO(post);
                    postDTO.isLikedByCurrentUser = isLiked;
                    return postDTO;
                })
                .collect(Collectors.toList());

        return new PagedResults<>(posts, posts.size());

    }

    @Override
    @Transactional(readOnly = true)
    public PagedResults<UserDTO> getUserFollowing(int userId) {
        List<User> followingUsers = getFollowing(userId);

        if(followingUsers.isEmpty()){
            return new PagedResults<>(Collections.emptyList(), 0);
        }

        List<UserDTO> followingUsersDto = userDTOMapper.toUserDTOList(followingUsers);

        return new PagedResults<>(followingUsersDto, followingUsersDto.size());

    }

    @Override
    @Transactional(readOnly = true)
    public PagedResults<UserDTO> getUserFollowers(int userId) {
        List<User> followers = getFollowers(userId);

        if(followers.isEmpty()){
            return new PagedResults<>(Collections.emptyList(), 0);
        }

        List<UserDTO> followersDto = userDTOMapper.toUserDTOList(followers);

        return new PagedResults<>(followersDto, followersDto.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResults<PostDTO> getPostsByUser(int userId) {
        User user = this.findById(userId);

        List<PostDTO> posts = user.getPosts()
                .stream()
                .sorted(Comparator.comparing(Post::getCreationDateTime).reversed())
                .map(PostDTO :: new)
                .collect(Collectors.toList());

        return new PagedResults<>(posts, posts.size());
    }


    private List<User> getFollowing(int userId){
        User user = this.findById(userId);

        return user.getFollowing()
                .stream()
                .map(Follow::getFollowed)
                .collect(Collectors.toList());
    }

    private List<User> getFollowers(int userId){
        User user = this.findById(userId);

        return user.getFollowers() //mappedBy = "followed"
                .stream()
                .map(Follow :: getFollower)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User updateUserPassword(int userId, String newPassword) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        Date now = new Date();
        long timeWithoutMillis = (now.getTime() / 1000) * 1000;
        user.setLastPasswordResetDate(new Date(timeWithoutMillis));
        userRepository.save(user);
        entityManagerr.flush(); // ⬅️ Forsira upis u bazu odmah

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found after update"));
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


    @Override
    public List<User> findAllByIds(List<Integer> ids){
        return this.userRepository.findAllByIdIn(ids);
    }


}