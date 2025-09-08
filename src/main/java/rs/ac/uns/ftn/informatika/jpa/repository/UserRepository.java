package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.model.User;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameOptional(@Param("username")String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmailOptional(@Param("email") String email);


    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    List<Post> findPostsByUserId(int userId);


    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.id <> :excludedId")
    Page<User> getUsersByRoleNameExcludingId(@Param("roleName") String roleName, @Param("excludedId") int excludedId, Pageable pageable);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findAllByIdIn(List<Integer> ids);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name <> 'ROLE_ADMIN'")
    List<User> findAllNonAdminUsers();

    List<User> findByEnabledFalseAndCreatedAtBefore(LocalDateTime dateTime);

}


