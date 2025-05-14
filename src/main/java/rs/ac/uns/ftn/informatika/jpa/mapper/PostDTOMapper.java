package rs.ac.uns.ftn.informatika.jpa.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostDTOMapper {
    private final ModelMapper modelMapper;

    public PostDTOMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public PostDTO fromPostToDTO(Post post) {
        return new PostDTO(post);
    }

    public Post fromDTOToPost(PostDTO postDTO) {
        return modelMapper.map(postDTO, Post.class);
    }

    public List<PostDTO> toPostDTOList(List<Post> posts) {
        return posts.stream()
                .map(PostDTO::new).collect(Collectors.toList());
    }
}
