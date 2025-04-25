package rs.ac.uns.ftn.informatika.jpa.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.xml.crypto.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "Description")
    private String description;

    @Column(name = "ImagePath")
    private String imagePath;

    @Column(name = "CreationDateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationDateTime;

    @Column(name= "LikeCount" )
    private int likeCount;

    @Embedded
    @JsonIgnore
    private Location location;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "Id")
    @JsonIgnore
    private User user;


    public Post() {
        super();
    }

    public Post(User user, String description, String imagePath, LocalDateTime creationDateTime, Location location) {
        this.user = user;
        this.description = description;
        this.imagePath = imagePath;
        this.creationDateTime = creationDateTime;
        this.location = location;
    }

    public Post(String description, String imagePath, LocalDateTime creationDateTime, Double latitude, Double longitude) {
        this.description = description;
        this.imagePath = imagePath;
        this.creationDateTime = creationDateTime;
        this.location = new Location(latitude, longitude);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
