package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rs.ac.uns.ftn.informatika.jpa.dto.CreatePostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserSearchCriteria;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.Role;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResults.PagedResults;

import java.util.List;

public interface UserService {
    User findById(int id);
    User findByUsername(String username);
    List<User> findAll ();
    User save(UserDTO userDTO);
    User findByEmail(String email);
    User updateUser(int id, UserDTO user);
    List<Post> getAllPostsByUser(int userId);
    Page<UserDTO> getUsersExcludingAdmin(int adminId, Pageable pageable);
    Page<UserDTO> searchUsers(UserSearchCriteria criteria, Pageable pageable, int adminId);

    List<UserDTO> findAllContainingUsername(String username);
    PagedResults<PostDTO> getFollowingPosts(int userId);
    PagedResults<UserDTO> getUserFollowing(int userId);
    PagedResults<UserDTO> getUserFollowers(int userId);
    PagedResults<PostDTO> getPostsByUser(int userId);
    void updateUserPassword(int userId, String newPassword) throws Exception;
    boolean verifyPassword(int userId, String currentPassword);
}
