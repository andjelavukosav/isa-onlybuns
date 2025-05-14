package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.jpa.model.Follow;
import rs.ac.uns.ftn.informatika.jpa.model.User;

public interface FollowRepository extends JpaRepository<Follow, Integer> {
    boolean existsByFollowerAndFollowed(User follower, User followed);
    Follow findByFollowerAndFollowed(User follower, User followed);
}
