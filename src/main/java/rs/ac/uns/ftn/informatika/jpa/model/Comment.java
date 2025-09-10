package rs.ac.uns.ftn.informatika.jpa.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "creationDateTime", nullable = false)
    private LocalDateTime creationDateTime;

    public Comment() {}

    public Comment(String text, User user, Post post) {
        this.text = text;
        this.user = user;
        this.post = post;
        this.creationDateTime = LocalDateTime.now();
    }

    public Integer getId() { return id;}

    public void setId(Integer id) { this.id = id; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Post getPost() { return post; }

    public void setPost(Post post) { this.post = post; }

    public LocalDateTime getCreationDateTime() { return creationDateTime; }

    public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }
}
