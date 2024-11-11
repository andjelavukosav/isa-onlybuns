package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rs.ac.uns.ftn.informatika.jpa.dto.JwtAuthenticationRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserTokenStateDTO;
import rs.ac.uns.ftn.informatika.jpa.exception.ResourceConflictException;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.service.EmailSenderService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;
import rs.ac.uns.ftn.informatika.jpa.util.TokenUtils;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailSenderService emailService;


    @PostMapping("/login")
    public ResponseEntity<UserTokenStateDTO> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequestDTO authenticationRequest, HttpServletResponse response) {

        // Authenticate using email and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtain the user object from the authentication
        User user = (User) authentication.getPrincipal();

        // Check if the user is enabled (i.e., account is verified)
        if (!user.isEnabled()) {
            // If user is not enabled, return a forbidden response
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UserTokenStateDTO("Account not verified. Please check your email for activation link.", 0));
        }

        // Generate the JWT using the user's email
        String jwt = tokenUtils.generateToken(user.getEmail());
        int expiresIn = tokenUtils.getExpiredIn();

        return ResponseEntity.ok(new UserTokenStateDTO(jwt, expiresIn));
    }




    @PostMapping("/signup")
    public ResponseEntity<String> addUser(@RequestBody UserDTO userRequest) {
        User existUser = this.userService.findByEmail(userRequest.getEmail());

        if (existUser != null) {
            // Return a response with a conflict message if the email already exists
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        }

        // Continue with saving the new user if email doesn't exist
        userRequest.setEnabled(false);
        User user = this.userService.save(userRequest);

        String activationLink = "http://localhost:8080/auth/verify?email=" + user.getEmail();

        try {
            emailService.sendVerificationEmail(userRequest,activationLink);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("User created successfully", HttpStatus.CREATED);
    }

    @GetMapping(value = "/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("email") String email) {
        // Find the user by email
        User user = userService.findByEmail(email);

        // Check if the user exists
        if (user != null) {
            // Map the User entity to UserDTO
            UserDTO userDTO = UserDTOMapper.fromUsertoDTO(user);

            // Update the verification status in the UserDTO
            userDTO.setEnabled(true);

            // Update the user with the new UserDTO
            userService.updateUser(userDTO.getId(), userDTO);  // Ensure updateUser accepts a UserDTO

            // Return a success response
            return new ResponseEntity<>("Success - Activation", HttpStatus.ACCEPTED);
        }

        // Return a failure response if the user is not found
        return new ResponseEntity<>("Unsuccessful Activation", HttpStatus.NOT_FOUND);
    }

}