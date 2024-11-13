package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findAll();
    Post save(Post post);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    long countPostByUser(int userId);

    Post findById(int id);
}
