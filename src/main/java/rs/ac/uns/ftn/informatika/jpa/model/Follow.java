package rs.ac.uns.ftn.informatika.jpa.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Entity
public class Follow {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne()
    @JoinColumn(name = "follower_id", referencedColumnName = "Id", nullable = false)
    private User follower;

    @ManyToOne()
    @JoinColumn(name = "followed_id", referencedColumnName = "Id", nullable = false)
    private User followed;

    @Column(name = "follow_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date followTime;

    public Follow() {}

    public Follow(User follower, User followed) {
        this.follower = follower;
        this.followed = followed;
        this.followTime = new Date();
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public User getFollower() { return follower; }

    public void setFollower(User follower) { this.follower = follower; }

    public User getFollowed() { return followed; }

    public void setFollowed(User followed) { this.followed = followed; }

    public Date getFollowTime() { return followTime; }

    public void setFollowTime(Date followTime) { this.followTime = followTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Follow follow = (Follow) o;
        return Objects.equals(follower, follow.follower) &&
                Objects.equals(followed, follow.followed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(follower, followed);
    }


}
