package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.User;

import java.util.List;

public interface UserService {
    User findById(int id);
    User findByUsername(String username);
    List<User> findAll ();
    User save(UserDTO userDTO);
    User findByEmail(String email);
    User updateUser(int id, UserDTO user);
}
