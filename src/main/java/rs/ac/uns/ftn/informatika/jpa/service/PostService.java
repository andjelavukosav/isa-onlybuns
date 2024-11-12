package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.util.List;

public interface PostService {
    List<Post> findAll();
    Post save(PostDTO postDTO);
    PostDTO getPostById(Integer id);
}
