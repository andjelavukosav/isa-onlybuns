package rs.ac.uns.ftn.informatika.jpa.dto;

import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PostDTO {
    public int id;
    public int userId;
    public String description;
    public LocalDateTime creationDateTime;
    public String imagePath;
    public LocationDTO location;
    public String username;
    public MultipartFile image;

    public PostDTO() {}

    public PostDTO(int id, int userId, String description, LocalDateTime creationDateTime, String imagePath, LocationDTO location, String username) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.creationDateTime = creationDateTime;
        this.imagePath = imagePath;
        this.location = location;
        this.username = username;
    }

    public PostDTO(Post post) {
        this.id = post.getId();
        this.userId = post.getUserId();
        this.description = post.getDescription();
        this.location = new LocationDTO(post.getLocation());
        this.imagePath = post.getImagePath();
        this.username = post.getUsername();
    }
}
