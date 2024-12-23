package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.util.List;

public interface PostService {
    List<Post> findAll();
    Post save(PostDTO postDTO);
    PostDTO getPostById(Integer id);
    public long getPostCountForUser(int userId);
    Post findById(int id);
    Post update(PostDTO postDTO);
    boolean delete(int postId, int userId);

    }
