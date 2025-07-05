package rs.ac.uns.ftn.informatika.jpa.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    private Integer id;
    private String text;
    private Integer userId;
    private Integer postId;
    private LocalDateTime creationDateTime;
    private String username;

    public CommentDTO() {}

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public Integer getUserId() { return userId; }

    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getPostId() { return postId; }

    public void setPostId(Integer postId) { this.postId = postId; }

    public LocalDateTime getCreationDateTime() { return creationDateTime; }

    public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }
}
