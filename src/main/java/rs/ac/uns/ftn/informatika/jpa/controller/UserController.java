package rs.ac.uns.ftn.informatika.jpa.controller;

import org.hibernate.Hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.dto.*;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResults.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.FollowService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class UserController {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserDTOMapper userDTOMapper;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public User loadById(@PathVariable int userId) {
        return this.userService.findById(userId);
    }


    @GetMapping("/user/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> loadAll() {
        return this.userService.findAll();
    }

    @GetMapping("/whoami")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Transactional
    public User user() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return  this.userService.findByEmail(user.getEmail());
    }


    @GetMapping("/foo")
    public Map<String, String> getFoo() {
        Map<String, String> fooObj = new HashMap<>();
        fooObj.put("foo", "bar");
        return fooObj;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "5")int size,
            @RequestParam(defaultValue = "email")String sortBy,
            @RequestParam(defaultValue = "asc")String direction,
            Principal principal) {

        UserDTO admin = userDTOMapper.fromUsertoDTO(userService.findByUsername(principal.getName()));

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> usersPage = userService.getUsersExcludingAdmin(admin.getId(), pageable);

        return  usersPage.getTotalElements() == 0 ?
                ResponseEntity.noContent().build() : ResponseEntity.ok(usersPage);

    }

    @PostMapping(value = "/users/search",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestBody(required = false) UserSearchCriteria criteria,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "5")int size,
            @RequestParam(defaultValue = "email")String sortBy,
            @RequestParam(defaultValue = "asc")String direction,
            Principal principal) {

        UserDTO admin = userDTOMapper.fromUsertoDTO(userService.findByUsername(principal.getName()));

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> usersPage = userService.searchUsers(criteria, pageable, admin.getId());

        return  ResponseEntity.ok().body(usersPage);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int userId) {
         User user = userService.findById(userId);
        if (user != null) {
            return ResponseEntity.ok(userDTOMapper.fromUsertoDTO(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/update/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<String> update(
            @PathVariable int userId,
            @RequestBody UserDTO updateUser,
            Principal principal) {

        User authenticatedUser = userService.findByUsername(principal.getName());

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        // Provera: korisnik može menjati samo svoju lozinku ili admin može menjati bilo čiju
        if (authenticatedUser.getId() != userId && !authenticatedUser.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this password.");
        }

        try {
            userService.updateUser(userId, updateUser);
            return ResponseEntity.ok("Password updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update password.");
        }

    }

    @GetMapping("/users/searchBy")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserDTO>> searchUsersByUsername(@RequestParam String username) {

        List<UserDTO> results = userService.findAllContainingUsername(username);

        return ResponseEntity.ok(results);

    }


    @PostMapping(value = "/users/follow",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<UserFollowStateDTO> followUser(@RequestBody FollowRequest request) {
       
        UserFollowStateDTO result = followService.followUser(request.getFollowerId(), request.getFollowedId());

        return ResponseEntity.ok().body(result);

    }

    @PostMapping(value="/users/unfollow",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserFollowStateDTO> unfollowUser(@RequestBody FollowRequest request) {

        UserFollowStateDTO result = followService.unfollowUser(request.getFollowerId(), request.getFollowedId());

        return ResponseEntity.ok().body(result);

    }

    @GetMapping("/users/{followerId}/is-following/{followedId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Map<String, Boolean>> checkFollowing(@PathVariable("followerId")int followerId, @PathVariable("followedId")int followedId) {

        boolean isFollowing = followService.isFollowing(followerId, followedId);

        Map<String, Boolean> response = new HashMap<>();

        response.put("isFollowing", isFollowing);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/users/following-posts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResults<PostDTO>> getFollowingPosts(Principal principal){

        UserDTO currentUser = userDTOMapper.fromUsertoDTO(userService.findByUsername(principal.getName()));

        PagedResults<PostDTO> results = userService.getFollowingPosts(currentUser.getId());

        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/users/{userId}/following")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PagedResults<UserDTO>> getUserFollowing(@PathVariable("userId")int userId) {

        PagedResults<UserDTO> followingUsers = userService.getUserFollowing(userId);

        if (followingUsers.getResults().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(followingUsers);
    }

    @GetMapping("/users/{userId}/followers")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PagedResults<UserDTO>> getUserFollowers(@PathVariable("userId")int userId) {

        PagedResults<UserDTO> results = userService.getUserFollowers(userId);

        if(results.getResults().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/user/{userId}/posts")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PagedResults<PostDTO>> getUserPosts(@PathVariable("userId")int userId) {

        PagedResults<PostDTO> posts = userService.getPostsByUser(userId);

        return ResponseEntity.ok().body(posts);
    }


    @PutMapping("/users/update-password/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<String> updatePassword(
            @PathVariable int userId,
            @RequestBody String newPassword,
            Principal principal) {

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password cannot be empty.");
        }

        User authenticatedUser = userService.findByUsername(principal.getName());

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        // Provera: korisnik može menjati samo svoju lozinku ili admin može menjati bilo čiju
        if (authenticatedUser.getId() != userId && !authenticatedUser.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this password.");
        }

        try {
            userService.updateUserPassword(userId, newPassword);
            return ResponseEntity.ok("Password updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update password.");
        }
    }

    @PostMapping("/users/verify-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Boolean> verifyPassword(@RequestBody Map<String, String> request, Principal principal) {
        String currentPassword = request.get("currentPassword");
        int userId = Integer.parseInt(request.get("userId"));

        // Provera autentifikacije korisnika
        User authenticatedUser = userService.findByUsername(principal.getName());
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        // Korisnik može proveriti samo svoju lozinku ili admin može proveriti bilo čiju
        if (authenticatedUser.getId() != userId && !authenticatedUser.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }

        boolean isPasswordValid = userService.verifyPassword(userId, currentPassword);
        return ResponseEntity.ok(isPasswordValid);
    }

    @GetMapping("/users/location/{userId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Map<String, Double>> getUserLocation(@PathVariable int userId) {
        // Simulacija dohvaćanja koordinata iz baze
        User user = userService.findById(userId);
        Map<String, Double> location = new HashMap<>();
        location.put("latitude", user.getAddress().getLocation().getLatitude());
        location.put("longitude", user.getAddress().getLocation().getLongitude());
        return ResponseEntity.ok(location);
    }
}
