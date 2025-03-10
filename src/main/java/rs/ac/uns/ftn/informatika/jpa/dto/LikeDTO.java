package rs.ac.uns.ftn.informatika.jpa.dto;

import java.time.LocalDateTime;

public class LikeDTO {
    private Integer id;
    private LocalDateTime creationDateTime;
    private Integer userId;      // ID korisnika koji je lajkovao
    private Integer postId;      // ID objave koja je lajkovana

    public LikeDTO(Integer id, LocalDateTime creationDateTime) {
        this.id = id;
        this.creationDateTime = creationDateTime;
    }

    public LikeDTO() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }
}
