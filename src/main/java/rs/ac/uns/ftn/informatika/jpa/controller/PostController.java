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
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResult.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PostService postService;

    @Autowired
    private LikeService likeService;

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

    @Operation(description = "Get all posts without sort", method = "GET")
    @GetMapping(value = "/allPosts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getAllPostsWithoutSort() {
        List<Post> posts = postService.findAll();

        List<PostDTO> postsDTO = posts.stream()
                .map(PostDTO::new)
                .collect(Collectors.toList());

        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(posts.size());
        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }

    @Operation(description = "Get all posts with the most likes in the last seven days", method = "GET")
    @GetMapping(value = "/allPostsMostPopular", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getAllPostsMostPopular() {
        List<Post> posts = postService.findAll();  // Dohvati sve postove
        List<PostDTO> postsDTO = new ArrayList<>();

        // Datum koji predstavlja pre 7 dana
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Mapiraćemo Post objekte na broj lajkova
        Map<Post, Long> postLikeCountMap = new HashMap<>();

        // Iteriraj kroz sve postove i broj lajkove u poslednjih 7 dana
        for (Post post : posts) {
            // Dobavi lajkove za trenutni post
            List<LikeDTO> likes = this.likeService.findLikesByPostId(post.getId());

            //List<Like> likes = post.getLikes();  // Pretpostavljamo da post ima metodu getLikes()

            // Filtriraj lajkove koji su postavljeni u poslednjih 7 dana
            long likeCountLast7Days = likes.stream()
                    .filter(like -> like.getCreationDateTime().isAfter(sevenDaysAgo))  // Filter za lajkove poslednjih 7 dana
                    .count();

            // Dodaj broj lajkova u mapu
            postLikeCountMap.put(post, likeCountLast7Days);
        }

        // Sortiraj postove po broju lajkova u poslednjih 7 dana
        List<Post> sortedPosts = postLikeCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))  // Sortiraj po broju lajkova
                .map(Map.Entry::getKey)  // Uzmi samo postove (ne njihove brojeve lajkova)
                .collect(Collectors.toList());

        // Uzmi samo top 5 postova sa najviše lajkova
        List<Post> topPosts = sortedPosts.stream().limit(5).collect(Collectors.toList());

        // Konvertuj postove u DTO
        for (Post post : topPosts) {
            PostDTO postDTO = new PostDTO(post);
            postsDTO.add(postDTO);
        }

        // Pripremi pagirane rezultate
        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(postsDTO.size());  // Broj rezultata je 5 (top 5)

        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }

    @Operation(description = "Get the top 10 posts with the most likes ever", method = "GET")
    @GetMapping(value = "/top10PostsMostPopular", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getTop10PostsMostPopular() {
        List<Post> posts = postService.findAll();  // Dohvati sve postove
        List<PostDTO> postsDTO = new ArrayList<>();

        // Mapiraćemo Post objekte na broj lajkova
        Map<Post, Long> postLikeCountMap = new HashMap<>();

        // Iteriraj kroz sve postove i broj lajkove
        for (Post post : posts) {
            // Dobavi lajkove za trenutni post
            List<LikeDTO> likes = this.likeService.findLikesByPostId(post.getId());

            // Dobavi ukupan broj lajkova za trenutni post
            long likeCount = likes.stream().count();  // Ukupan broj lajkova za post

            // Dodaj broj lajkova u mapu
            postLikeCountMap.put(post, likeCount);
        }

        // Sortiraj postove po broju lajkova (od najviše ka najmanje)
        List<Post> sortedPosts = postLikeCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))  // Sortiraj po broju lajkova
                .map(Map.Entry::getKey)  // Uzmi samo postove (ne njihove brojeve lajkova)
                .collect(Collectors.toList());

        // Uzmi samo top 10 postova sa najviše lajkova
        List<Post> topPosts = sortedPosts.stream().limit(10).collect(Collectors.toList());

        // Konvertuj postove u DTO
        for (Post post : topPosts) {
            PostDTO postDTO = new PostDTO(post);
            postsDTO.add(postDTO);
        }

        // Pripremi pagirane rezultate
        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(postsDTO.size());  // Broj rezultata je 10 (top 10)

        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }


    @Operation(description = "Get all posts from the last month", method = "GET")
    @GetMapping(value = "/allPostsLastMonth", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getAllPostsLastMonth() {
        // Dohvatanje svih postova
        List<Post> posts = postService.findAll();

        // Datum pre mesec dana
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        // Filtriranje postova koji su kreirani u poslednjih mesec dana
        List<Post> postsLastMonth = posts.stream()
                .filter(post -> post.getCreationDateTime().isAfter(oneMonthAgo))
                .collect(Collectors.toList());

        // Konvertovanje postova u DTO
        List<PostDTO> postsDTO = postsLastMonth.stream()
                .map(PostDTO::new)
                .collect(Collectors.toList());

        // Kreiranje paginiranih rezultata
        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(postsLastMonth.size());

        // Vraćanje odgovora sa filtriranim postovima
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


    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Integer postId) {
        PostDTO postDTO = postService.getPostById(postId);
        return ResponseEntity.ok(postDTO);
    }

    @Operation(description = "Get posts by user ID", method = "GET")
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getPostsByUserId(@PathVariable int userId) {
        // Fetch all posts for the given user ID
        List<Post> userPosts = postService.findByUserId(userId);

        // Sort the posts by creationDateTime in descending order (newest first)
        userPosts.sort((p1, p2) -> p2.getCreationDateTime().compareTo(p1.getCreationDateTime()));

        // Map posts to PostDTO
        List<PostDTO> postsDTO = userPosts.stream()
                .map(PostDTO::new)
                .collect(Collectors.toList());

        // Create paged results
        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(userPosts.size());

        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }


    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Long> getPostCountForUser(@PathVariable int userId) {
        Long count = postService.getPostCountForUser(userId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{postId}/likes/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likePost(@PathVariable int postId, @PathVariable int userId) {
        try {
            Like like = new Like();
            like.setCreationDateTime(LocalDateTime.now());
            like.setPost(postService.findById(postId));
            like.setUser(userService.findById(userId));
            this.likeService.save(like);
            PostDTO postDTO = new PostDTO(postService.findById(postId));
            postDTO.id = postId;
            postService.update(postDTO);
            return ResponseEntity.ok().build();
        } catch (ConfigDataResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while liking the post.");
        }

    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable int postId, @RequestParam int userId) {
        boolean isDeleted = postService.delete(postId, userId);

        if (isDeleted) {
            return ResponseEntity.ok().body("Post deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this post.");
        }
    }

    /*@PutMapping("/")
    public ResponseEntity<?> updatePost(@RequestBody PostDTO postRequest){
        PostDTO updatedPost =  new PostDTO(this.postService.update(postRequest));
        if (updatedPost != null) {
            return ResponseEntity.ok(updatedPost); // Vraćamo PostDTO kao odgovor
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this post.");
        }
    }*/

    @PutMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostDTO> updatePost(
            @RequestParam("id") int id,
            @RequestParam("description") String description,
            @RequestParam(value = "likeCount", required = false, defaultValue = "0") int likeCount,
            @RequestParam(value = "location.latitude", required = false) Double latitude,
            @RequestParam(value = "location.longitude", required = false) Double longitude,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imagePath", required = false) String imagePath,
            @RequestParam("creationDateTime") String creationDateTime,
            @RequestParam("userId") int userId
    ) throws IOException {

        // Pronalazi postojeći post po ID-u
        Post existingPost = postService.findById(id);
        if (existingPost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Ažurira polja posta
        existingPost.setDescription(description);
        existingPost.setCreationDateTime(LocalDateTime.parse(creationDateTime));

        // Ažurira korisnika po userId
        User user = userService.findById(userId);
        if (user != null) {
            existingPost.setUser(user);
        }

        // Ažurira lokaciju ako su latitude i longitude prisutni
        if (latitude != null && longitude != null) {
            existingPost.setLocation(new Location(latitude, longitude));
        }

        // Obrada slike ako je prisutna
        if (imageFile != null && !imageFile.isEmpty()) {
            String newImagePath = saveImage(imageFile);  // Metoda za čuvanje slike na serveru
            existingPost.setImagePath(newImagePath);
        } else {
            existingPost.setImagePath(imagePath);  // Postavlja imagePath ako je prosleđen
        }

        // Čuvanje ažuriranog posta
        PostDTO updatedPostDTO = new PostDTO(postService.save(new PostDTO(existingPost)));
        return new ResponseEntity<>(updatedPostDTO, HttpStatus.OK);
    }


}
