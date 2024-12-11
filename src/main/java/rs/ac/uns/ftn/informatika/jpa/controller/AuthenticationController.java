package rs.ac.uns.ftn.informatika.jpa.controller;

import io.github.resilience4j.ratelimiter.RateLimiter;
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
import rs.ac.uns.ftn.informatika.jpa.service.RateLimiterService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;
import rs.ac.uns.ftn.informatika.jpa.util.TokenUtils;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private RateLimiterService rateLimiterService;

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);


    @PostMapping("/login")
    public ResponseEntity<UserTokenStateDTO> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequestDTO authenticationRequest,
            HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        RateLimiter rateLimiter = rateLimiterService.getRateLimiter(ipAddress);

        try {
            return RateLimiter.decorateCheckedSupplier(rateLimiter, () -> {
                // Logika autentifikacije
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authenticationRequest.getEmail(),
                                authenticationRequest.getPassword()
                        )
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                User user = (User) authentication.getPrincipal();

                if (!user.isEnabled()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new UserTokenStateDTO("Account not verified. Please check your email for activation link.", 0));
                }

                String jwt = tokenUtils.generateToken(user.getEmail());
                int expiresIn = tokenUtils.getExpiredIn();

                return ResponseEntity.ok(new UserTokenStateDTO(jwt, expiresIn));
            }).apply(); // Koristimo apply() za rukovanje CheckedSupplier
        } catch (Throwable throwable) {
            // Rukovanje izuzetkom
            if (throwable instanceof RequestNotPermitted) {
                // Logovanje kada je premašeno ograničenje
                LOG.warn("Too many login attempts from IP: {}", ipAddress);
                System.out.println("Too many login attempts from IP: " + ipAddress); // Ispis u konzolu

                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new UserTokenStateDTO("Too many login attempts. Please try again later.", 0));
            }
            throw new RuntimeException(throwable); // Ili prilagodite rukovanje
        }
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