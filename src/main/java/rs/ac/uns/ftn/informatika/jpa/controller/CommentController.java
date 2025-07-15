package rs.ac.uns.ftn.informatika.jpa.controller;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.CommentDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Comment;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.service.CommentService;
import rs.ac.uns.ftn.informatika.jpa.service.RateLimiterService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RateLimiterService rateLimiterService;


    /**
     * Vraćanje svih komentara za objavu, sortirano od najnovijeg.
     */
    @Operation(description = "Get all comments for a post (sorted by newest first)", method = "GET")
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsForPost(@PathVariable Integer postId) {
        List<CommentDTO> comments = commentService.findCommentsByPostId(postId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }


    @PostMapping(value = "/{postId}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addComment(
            @PathVariable Integer postId,
            @RequestBody CommentDTO commentDTO,
            Principal principal) {

        System.out.println("⬅️ Pozvan addComment endpoint za postId = " + postId);

        RateLimiter limiter = rateLimiterService.getRateLimiter(principal.getName());
        boolean allowed = limiter.acquirePermission();
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Prekoračili ste maksimalan broj komentara u minuti");
        }

        if (commentDTO.getText() == null || commentDTO.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Comment text cannot be empty.");
        }

        User currentUser = userService.findByUsername(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        try {
            Comment savedComment = commentService.addComment(commentDTO.getText(), currentUser.getId(), postId);

            // Mapiranje entiteta u DTO za odgovor
            CommentDTO responseDTO = new CommentDTO();
            responseDTO.setId(savedComment.getId());
            responseDTO.setText(savedComment.getText());
            responseDTO.setUserId(savedComment.getUser().getId());
            responseDTO.setPostId(savedComment.getPost().getId());
            responseDTO.setCreationDateTime(savedComment.getCreationDateTime());
            responseDTO.setUsername(savedComment.getUser().getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


}
