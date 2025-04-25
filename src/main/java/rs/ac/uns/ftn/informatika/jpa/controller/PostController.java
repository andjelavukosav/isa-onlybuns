package rs.ac.uns.ftn.informatika.jpa.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.dto.CreatePostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.PostDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResult.PagedResults;

import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;


import javax.validation.ConstraintViolation;

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

    @Autowired
    private UserDTOMapper userDTOMapper;
    @Autowired
    private PostDTOMapper postDTOMapper;
    @Autowired
    private PostRepository postRepository;



    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PostDTO>> getPostsByUser(@PathVariable int userId) {
        /*User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Post> posts = new ArrayList<>(user.getPosts());*/
        List<PostDTO> results = postService.findByUser(userId);
        return new ResponseEntity<>(results, HttpStatus.OK);
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
        List<Post> posts = postService.findAllPosts();

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
        List<Post> topPosts = postService.getAllPostsMostPopularLast7Days();

        // Konverzija u DTO u kontroleru
        List<PostDTO> postsDTO = topPosts.stream()
                .map(PostDTO::new)
                .collect(Collectors.toList());

        // Priprema pagiranih rezultata
        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(postsDTO.size());

        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }

    @Operation(description = "Get the top 10 posts with the most likes ever", method = "GET")
    @GetMapping(value = "/top10PostsMostPopular", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getTop10PostsMostPopular() {
        List<Post> topPosts = postService.getTop10PostsMostPopular();

        // Konverzija u DTO u kontroleru
        List<PostDTO> postsDTO = topPosts.stream()
                .map(PostDTO::new)
                .collect(Collectors.toList());

        // Priprema pagiranih rezultata
        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(postsDTO.size());

        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }


    @Operation(description = "Get all posts from the last month", method = "GET")
    @GetMapping(value = "/allPostsLastMonth", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<PostDTO>> getAllPostsLastMonth() {

        postService.removeFromCache();


        List<Post> posts = postService.getAllPostsLastMonth();
        // Konverzija u DTO u kontroleru
        List<PostDTO> postsDTO = posts.stream()
                .map(PostDTO::new)
                .collect(Collectors.toList());

        PagedResults<PostDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(postsDTO.size());

        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }

    @Operation(description = "Create a new post", method = "POST")
    @PostMapping(value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPost(
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Principal principal) {

        CreatePostDTO postRequest = new CreatePostDTO(description, imageFile, latitude, longitude);

        if(!postRequest.isValid()){
            return new ResponseEntity<>("Missing input data.",HttpStatus.BAD_REQUEST);
        }

        postService.removeFromCache();

        System.out.println("REQUEST: description: " + description + ", longitude: " + longitude + ", latitude: " + latitude + ", imageFile: " + imageFile);

        int userId = this.userService.findByUsername(principal.getName()).getId();

        PostDTO result = postService.createPost(postRequest, userId);

        postService.clearCache();
        return new ResponseEntity<>(result, HttpStatus.CREATED);
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

            // Ažuriraj leaderboard (top 5 postova)
            postService.likePost(postId, userId);  // Ova metoda ažurira leaderboard i keš

            return ResponseEntity.ok().build();
        } catch (ConfigDataResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while liking the post.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") int postId, Principal principal) {

        int userId = this.userService.findByUsername(principal.getName()).getId();

        boolean isDeleted = postService.delete(postId, userId);

        if (isDeleted) {
            postService.removeFromCache();
            return ResponseEntity.ok().body("Post deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this post.");
        }
    }

    @PutMapping(
            value = {"/update/{id}"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable("id") int id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile){

        CreatePostDTO postRequest = new CreatePostDTO(description, imageFile, latitude, longitude);

        PostDTO updatedPost = postService.update(postRequest, id);

        this.postService.removeFromCache();

        return ResponseEntity.ok(updatedPost);

    }

    @Operation(
            description = "Get nearby posts based on user's location",
            method = "GET"
    )
    @GetMapping(
            value = {"/nearby"},
            produces = {"application/json"}
    )
    public ResponseEntity<PagedResults<PostDTO>> getNearbyPosts(@RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude, @RequestParam(value = "radius",defaultValue = "100000") double radius) {
        List<Post> nearbyPosts = this.postService.findNearbyPosts(latitude, longitude, radius);
        List<PostDTO> postsDTO = (List)nearbyPosts.stream().map(PostDTO::new).collect(Collectors.toList());
        PagedResults<PostDTO> pagedResults = new PagedResults();
        pagedResults.setResults(postsDTO);
        pagedResults.setTotalCount(postsDTO.size());
        return new ResponseEntity(pagedResults, HttpStatus.OK);
    }

}
