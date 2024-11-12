package rs.ac.uns.ftn.informatika.jpa.controller;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class UserController {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

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
    public User user(Principal user) {
        User currentUser = this.userService.findByUsername(user.getName());

        // Inicijalizujte lazy kolekcije (ako postoji potreba)
        Hibernate.initialize(currentUser.getFollowers());  // Inicijalizuje followers kolekciju

        return currentUser;
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



}
