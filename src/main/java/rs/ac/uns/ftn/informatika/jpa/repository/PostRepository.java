package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findAll();
    Post save(Post post);

    void deleteById(int postId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    long countPostByUser(int userId);

    Post findById(int id);

    List<Post> findByUserId(int userId);

    @Query("SELECT p FROM Post p WHERE p.id IN (SELECT l.post.id FROM Like l WHERE l.creationDateTime >= :sevenDaysAgo GROUP BY l.post.id ORDER BY COUNT(l.id) DESC)")
    List<Post> findTop5ByLikesInLast7Days(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    @Query("SELECT p FROM Post p LEFT JOIN Like l ON l.post = p GROUP BY p.id ORDER BY COUNT(l) DESC")
    List<Post> findTop10ByMostLikedAllTime();

    @Query("SELECT p FROM Post p WHERE p.creationDateTime >= :sevenDaysAgo ORDER BY p.creationDateTime DESC")
    List<Post> findTop5ByDateLast7Days(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    @Query("SELECT p FROM Post p WHERE 6371000 * acos(cos(radians(:latitude)) * cos(radians(p.location.latitude)) * cos(radians(p.location.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(p.location.latitude))) <= :radius")
    List<Post> findNearbyPosts(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("radius") double radius);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.likes WHERE p.id = :postId")
    Post findPostWithLikes(@Param("postId") int postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Post findByIdForUpdate(@Param("id") int id);


}

