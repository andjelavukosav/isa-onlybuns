package rs.ac.uns.ftn.informatika.jpa.controller;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

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
    public List<UserDTO> findRegisteredUsers(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Principal is null, user not authenticated.");
        }

        User adminUser = this.userService.findByUsername(principal.getName());
        if (adminUser == null) {
            throw new RuntimeException("User not found or does not have the required role.");
        }

        int adminId = adminUser.getId();
        return this.userService.findUsersByRoleExcludingAdmin(adminId);
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> searchUsers(
            @RequestParam(required = false)String firstName,
            @RequestParam(required = false)String lastName,
            @RequestParam(required = false)String email,
            @RequestParam(required = false)Long minPosts,
            @RequestParam(required = false)Long maxPosts,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            Principal principal) {

         if(principal == null) {
             throw new RuntimeException("Principal is null, user not authenticated.");
         }
         User adminUser = this.userService.findByUsername(principal.getName());
         if (adminUser == null) {
             throw new RuntimeException("User not found or does not have the required role.");
         }
         int adminId = adminUser.getId();

        List<String> validSortByFields = Arrays.asList("email", "followersCount");

        // Ako je parametar 'sortBy' null ili nije validan, postavi default vrednost
        if (sortBy == null || !validSortByFields.contains(sortBy)) {
            sortBy = "email"; // Default vrednost
        }

        // Kreiranje Sort objekta na osnovu parametara
        Sort sort = Sort.by(Sort.Order.asc(sortBy)); // Default je uzlazno sortiranje
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            sort = Sort.by(Sort.Order.desc(sortBy)); // Ako je "DESC", koristi silazno sortiranje
        }

        return this.userService.searchUsers(firstName, lastName, email, minPosts, maxPosts, adminId, sort);
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int userId) {
         User user = userService.findById(userId);
        if (user != null) {
            return ResponseEntity.ok(UserDTOMapper.fromUsertoDTO(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}
