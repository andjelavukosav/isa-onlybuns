package rs.ac.uns.ftn.informatika.jpa.dto;

import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class PostDTO {
    public int id;

    @NotBlank(message = "Description is required.")
    public String description;

    public LocalDateTime creationDateTime;

    @NotBlank
    public String imagePath;

    public LocationDTO location;

    @NotNull
    public MultipartFile image;

    public boolean isLikedByCurrentUser = false;

    public int likeCount;

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
        if (this.imagePath != null && !this.imagePath.startsWith("http")) {
            this.imagePath = "http://localhost:8080" + this.imagePath;
        }
        this.user = new UserDTO(post.getUser());
        this.creationDateTime = post.getCreationDateTime();
        this.likeCount = post.getLikeCount();
    }

    public UserDTO getUser() { return user; }

    public void setUser(UserDTO user) { this.user = user; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }

    public LocalDateTime getCreationDateTime() { return creationDateTime; }


}
