package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Like save(Like like);

    Like findByPostIdAndUserId(Integer postId, Integer userId);

    List<Like> findLikesByPostId(Integer postId);

    Like findById(int id);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    long countByPostId(@Param("postId") int postId);

    void deleteById(int id);

    @Query("SELECT l.post.id FROM Like l WHERE l.user.id = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") int userId);

}
