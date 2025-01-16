package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    @Autowired
    private LikeService likeService;

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



}
