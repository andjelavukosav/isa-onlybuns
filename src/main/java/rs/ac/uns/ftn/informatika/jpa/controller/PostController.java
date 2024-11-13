package rs.ac.uns.ftn.informatika.jpa.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResult.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Post> posts = new ArrayList<>(user.getPosts());
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }


    /*@GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.findAll();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }*/

    @Operation(description = "Get all posts", method = "GET")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getAllPosts() {
        List<Post> posts = postService.findAll();

        // Sort the posts by creationDateTime in descending order (newest first)
        posts.sort((p1, p2) -> p2.getCreationDateTime().compareTo(p1.getCreationDateTime()));

        List<PostDTO> postsDTO = posts.stream()
                .map(PostDTO::new)
                .collect(Collectors.toList());

        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(posts.size());
        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }


    @Operation(description = "Create a new post", method = "POST")
    @PostMapping(value = "/create", consumes = "multipart/form-data", produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> createPost(
            @RequestParam("description") String description,
            @RequestParam(value = "location.latitude", required = false) Double latitude,
            @RequestParam(value = "location.longitude", required = false) Double longitude,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Principal principal
    ) throws IOException {

        User user = this.userService.findByUsername(principal.getName());
        System.out.println(user.getEmail());
        if (user == null) {
            System.out.println("Korisnik nije pronađen.");
        }

        // Validacija inputa
        if (user.getId() <= 0 || description == null || description.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        // Kreiranje Post objekta
        Post post = new Post();
        post.setUser(user); // Postavljanje korisnika
        post.setDescription(description);
        post.setLikeCount(0);
        // Postavljanje lokacije ako je prisutna
        if (latitude != null && longitude != null) {
            post.setLocation(new Location(latitude, longitude));
        }

        // Obrada slike ako je prisutna
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);  // Metoda za čuvanje slike na serveru
            post.setImagePath(imagePath);
        }

        post.setCreationDateTime(LocalDateTime.now());


        // Čuvanje posta u bazi
        PostDTO postDTO = new PostDTO(post);
        post = postService.save(postDTO);


        return new ResponseEntity<>(postDTO, HttpStatus.CREATED);
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        // Definišite folder za čuvanje slika unutar statičkog direktorijuma
        String uploadDir = "uploads/images";
        Path uploadPath = Paths.get(uploadDir);

        // Kreirajte folder ako ne postoji
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generišite jedinstveno ime za fajl
        String fileName = UUID.randomUUID().toString() + "-" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Sačuvajte fajl u folder
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Vratite ime fajla koje će se koristiti za pristup slici
        return "/images/" + fileName;
    }


    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likePost(@PathVariable int postId) {
       try {
            Post post = postService.findById(postId);
            post.setLikeCount(post.getLikeCount() + 1);
            PostDTO postDTO = new PostDTO(post);
            postDTO.id = postId;
            postService.update(postDTO);
            return ResponseEntity.ok("Post liked successfully");
        } catch (ConfigDataResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while liking the post.");
        }

    }

}
