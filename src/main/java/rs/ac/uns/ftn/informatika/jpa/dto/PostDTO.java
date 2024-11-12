package rs.ac.uns.ftn.informatika.jpa.dto;

import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PostDTO {
    public int id;
    public String description;
    public LocalDateTime creationDateTime;
    public String imagePath;
    public LocationDTO location;
    public MultipartFile image;
    public UserDTO user;

    public PostDTO() {}

    public PostDTO(int id, String description, LocalDateTime creationDateTime, String imagePath, LocationDTO location) {
        this.id = id;
        this.description = description;
        this.creationDateTime = creationDateTime;
        this.imagePath = imagePath;
        this.location = location;
    }

    public PostDTO(Post post) {
        this.id = post.getId();
        this.description = post.getDescription();
        this.location = new LocationDTO(post.getLocation());
        this.imagePath = post.getImagePath();
        this.user = new UserDTO(post.getUser());
        this.creationDateTime = post.getCreationDateTime();
    }

    public UserDTO getUser() { return user; }
}
