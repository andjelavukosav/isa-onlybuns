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
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

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

    @GetMapping("/{postId}/{userId}")
    public ResponseEntity<Boolean> likePost(@PathVariable int postId, @PathVariable int userId) {
        boolean exists = false;
        LikeDTO existsLike = this.likeService.findLikeByPostIdAndUserId(postId, userId);
        if(existsLike != null) {
            exists=true;
        }
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/countLikes/{postId}")
    public ResponseEntity<Integer> getAll(@PathVariable int postId) {
        try {
            List<LikeDTO> likeDTOS = this.likeService.findLikesByPostId(postId);
            // Vraćamo broj lajkova, ako je lista prazna, broj je 0
            return ResponseEntity.ok(likeDTOS != null ? likeDTOS.size() : 0);
        } catch (Exception e) {
            // Ako se desi greška, vraćamo 0, a ne 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }

    @DeleteMapping("/unlike/{postId}/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteLike(@PathVariable int postId, @PathVariable int userId) {
        boolean isDeleted = likeService.delete(postId, userId);

        if (isDeleted) {
            return ResponseEntity.noContent().build(); // Uspešno brisanje (204 No Content)
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Like not found or user not authorized to delete this like."); // Greška (404 Not Found)
        }
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



}
