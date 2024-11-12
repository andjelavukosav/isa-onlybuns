package rs.ac.uns.ftn.informatika.jpa.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.User;

@Component
public class UserDTOMapper {
    private static ModelMapper modelMapper;

    @Autowired
    public UserDTOMapper(ModelMapper modelMapper) { this.modelMapper = modelMapper; }

    public static User fromDTOtoUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public static UserDTO fromUserToDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
}
