package rs.ac.uns.ftn.informatika.jpa.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.AddressDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserSearchCriteria;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.*;
import rs.ac.uns.ftn.informatika.jpa.pagedResults.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.repository.AddressRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.RoleService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;
import rs.ac.uns.ftn.informatika.jpa.specification.UserSpecification;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private PostService postService;

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(int id) throws AccessDeniedException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
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
        return userDTOMapper.toUserDTOList(userRepository.findByUsernameContaining(username));
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
                .map(PostDTO:: new)
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

}