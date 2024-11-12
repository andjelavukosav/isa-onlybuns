package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);


    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    List<Post> findPostsByUserId(int userId);


    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.id <> :excludedId")
    List<User> findAllByRoleNameExcludingId(@Param("roleName") String roleName, @Param("excludedId") int excludedId);


    @Query("SELECT DISTINCT u FROM User u LEFT JOIN Post p ON u.id = p.user.id " +
            "WHERE u.id <> :adminId " +
            "AND (:firstName IS NULL OR u.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR u.lastName LIKE %:lastName%) " +
            "AND (:email IS NULL OR u.email LIKE %:email%) " +
            "AND ((:minPosts IS NULL OR " +
            "(SELECT COUNT(p) FROM Post p WHERE p.user.id = u.id) >= :minPosts) " +
            "AND (:maxPosts IS NULL OR " +
            "(SELECT COUNT(p) FROM Post p WHERE p.user.id = u.id) <= :maxPosts))")
    List<User> searchUserBy(@Param("firstName") String firstName,
                            @Param("lastName") String lastName,
                            @Param("email") String email,
                            @Param("minPosts") Long minPosts,
                            @Param("maxPosts") Long maxPosts,
                            @Param("adminId") int adminId,
                            Sort sort);




}


