package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.Role;
import rs.ac.uns.ftn.informatika.jpa.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User findById(int id);
    User findByUsername(String username);
    Optional<User> findByUsernameWithLock(String username);

    List<User> findAll ();
    User save(UserDTO userDTO);
    User findByEmail(String email);
    User updateUser(int id, UserDTO user);
    List<Post> getAllPostsByUser(int userId);
    List<UserDTO> findUsersByRoleExcludingAdmin(int adminId);
    List<UserDTO> searchUsers(String firstName, String lastName, String email, Long minPosts, Long maxPosts, int adminId, Sort sort);
}
