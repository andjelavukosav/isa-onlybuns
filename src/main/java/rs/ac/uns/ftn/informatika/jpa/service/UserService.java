package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Role;
import rs.ac.uns.ftn.informatika.jpa.model.User;

import java.util.List;

public interface UserService {
    User findById(int id);
    User findByUsername(String username);
    List<User> findAll ();
    User save(UserDTO userDTO);
    List<UserDTO> findUsersByRoleExcludingAdmin(int adminId);
    List<User> searchUsers(String firstName, String lastName, String email);
}
