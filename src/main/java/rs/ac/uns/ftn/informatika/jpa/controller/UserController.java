package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

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
    public ResponseEntity<User> user(Principal user) {
        User userFind = this.userService.findByUsername(user.getName());

        // Check if the user was found
        if (userFind == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // or return an error message
        }

        User userByEmail = this.userService.findByEmail(userFind.getEmail());

        // Check if the user by email was found
        if (userByEmail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // or return an error message
        }

        return ResponseEntity.ok(userByEmail);
    }


    @GetMapping("/foo")
    public Map<String, String> getFoo() {
        Map<String, String> fooObj = new HashMap<>();
        fooObj.put("foo", "bar");
        return fooObj;
    }
}
