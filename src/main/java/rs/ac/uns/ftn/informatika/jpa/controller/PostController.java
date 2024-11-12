package rs.ac.uns.ftn.informatika.jpa.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

        List<PostDTO> postsDTO = new ArrayList<>();
        for (Post post : posts) {
            postsDTO.add(new PostDTO(post));
        }
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

    @Value("${upload.folder}")
    private String uploadFolder;

    private String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Fajl je prazan.");
        }

        // Provera tipa fajla
        String contentType = imageFile.getContentType();
        if (!contentType.startsWith("image")) {
            throw new IllegalArgumentException("Fajl nije validna slika.");
        }

        // Generisanje jedinstvenog imena fajla
        String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        fileName = fileName.replace(":", "-"); // Zameni ':' zbog problema na Windows-u

        // Putanja gde se fajl čuva
        Path imagePath = Paths.get(uploadFolder, fileName);

        // Čuvanje fajla
        Files.createDirectories(imagePath.getParent()); // Osiguraj da folder postoji
        Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName; // Vraća ime fajla koje se može koristiti za prikazivanje slike
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Integer postId) {
        PostDTO postDTO = postService.getPostById(postId);
        return ResponseEntity.ok(postDTO);
    }

}
