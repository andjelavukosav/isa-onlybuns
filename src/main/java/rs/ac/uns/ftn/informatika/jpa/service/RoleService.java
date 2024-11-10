package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.model.Role;
import java.util.List;

public interface RoleService {
    Role findById(int id);
    List<Role> findByName(String name);
}
