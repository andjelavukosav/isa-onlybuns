package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.jpa.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPostIdOrderByCreationDateTimeDesc(Integer postId);
    int countByUserIdAndCreationDateTimeAfter(int userId, LocalDateTime after);
}
