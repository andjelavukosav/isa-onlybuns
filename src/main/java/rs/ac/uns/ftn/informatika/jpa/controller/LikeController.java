package rs.ac.uns.ftn.informatika.jpa.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResult.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @PostMapping(value = "like-post/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likePost(@PathVariable int postId, Principal principal) {
        int userId = userService.findByUsername(principal.getName()).getId();

        boolean isPostLiked = postService.likePost(postId, userId);

        Map<String, String> response = new HashMap<>();

        if (isPostLiked) {
            response.put("message", "Post successfully liked.");
            return ResponseEntity.ok(response);
        }
        response.put("message", "Post is already liked by this user.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }



    @Operation(description = "Get the top 10 users who liked the most posts in the last 7 days", method = "GET")
    @GetMapping(value = "/top10UsersMostLikes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResults<UserDTO>> getTop10UsersMostLikes() {
        // Datum koji predstavlja pre 7 dana
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Dohvati sve lajkove
        List<LikeDTO> allLikes = likeService.findAll();

        // Mapiraćemo korisnike na broj lajkova koje su podelili u poslednjih 7 dana
        Map<Integer, Long> userLikeCountMap = new HashMap<>();

        // Iteriraj kroz sve lajkove
        for (LikeDTO like : allLikes) {
            // Filtriraj lajkove koji su postavljeni u poslednjih 7 dana
            if (like.getCreationDateTime().isAfter(sevenDaysAgo)) {
                // Ako je lajk postavljen u poslednjih 7 dana, povećaj broj lajkova korisnika
                userLikeCountMap.put(like.getUserId(), userLikeCountMap.getOrDefault(like.getUserId(), 0L) + 1);
            }
        }

        // Sortiraj korisnike po broju podeljenih lajkova (od najviše ka najmanje)
        List<Map.Entry<Integer, Long>> sortedUserLikes = userLikeCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))  // Sortiraj po broju lajkova
                .collect(Collectors.toList());

        // Uzmi samo top 10 korisnika sa najviše podeljenih lajkova
        List<Integer> topUserIds = sortedUserLikes.stream().limit(10).map(Map.Entry::getKey).collect(Collectors.toList());

        // Dohvati korisnike na osnovu njihovih ID-jeva
        List<UserDTO> topUsers = new ArrayList<>();
        for (Integer userId : topUserIds) {
            User user = userService.findById(userId);
            UserDTO userDTO = new UserDTO(user);
            topUsers.add(userDTO);
        }

        // Pripremi pagirane rezultate
        PagedResults<UserDTO> pagedResults = new PagedResults<>();
        pagedResults.setResults(topUsers);
        pagedResults.setTotalCount(topUsers.size());  // Broj rezultata je 10 (top 10)

        return new ResponseEntity<>(pagedResults, HttpStatus.OK);
    }

    @DeleteMapping(value = "/unlike-post/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unlikePost(@PathVariable int postId, Principal principal) {

        int userId = userService.findByUsername(principal.getName()).getId();

        boolean isPostUnliked = postService.unlikePost(postId, userId);

        Map<String, String> response = new HashMap<>();

        if (isPostUnliked) {
            response.put("message", "Post successfully unliked.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "An error has occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @GetMapping(value = "/post/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getLikesFromPost(@PathVariable int postId) {

        List<UserDTO> likesList = postService.getLikesFromPost(postId);

        return ResponseEntity.ok(likesList);
    }

    @GetMapping("/liked-post-ids")
    public List<Long> getLikedPostIds(Principal currentUser) {
        User authenticatedUser = userService.findByUsername(currentUser.getName());
        return likeService.getLikedPostIdsByUser(authenticatedUser.getId());
    }


}
