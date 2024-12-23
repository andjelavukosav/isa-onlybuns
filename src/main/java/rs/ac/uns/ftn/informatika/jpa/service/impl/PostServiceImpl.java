package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDTOMapper userDTOMapper;

    @Override
    public List<Post> findAll() throws AccessDeniedException {
        return postRepository.findAll();
    }

    @Override
    public Post save(PostDTO postRequest) {
        Post post = new Post();
        post.setId(postRequest.id);
        post.setDescription(postRequest.description);
        post.setCreationDateTime(postRequest.creationDateTime);
        post.setLocation(new Location(postRequest.location));
        post.setImagePath(postRequest.imagePath);
        post.setUser(UserDTOMapper.fromDTOtoUser(postRequest.getUser()));
        post.setLikeCount(postRequest.likeCount);
        return this.postRepository.save(post);
    }

    @Override
    public PostDTO getPostById(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        return new PostDTO(post);
    }

    @Override
    public long getPostCountForUser(int userId) {
        return postRepository.countPostByUser(userId);
    }

    @Override
    public Post findById(int id) {
        return this.postRepository.findById(id);
    }

    public Post update(PostDTO postRequest) {
        // Retrieve the post from the database


        Post post = this.postRepository.findById(postRequest.id);

        // Update fields as per the request
        post.setDescription(postRequest.description);
        post.setLocation(new Location(postRequest.location));  // Ensure location is mapped properly
        post.setImagePath(postRequest.imagePath);
        post.setCreationDateTime(postRequest.creationDateTime);
        // Only update the like count if explicitly specified (you may wish to exclude this for update consistency)

        post.setLikeCount(postRequest.likeCount);


        // Update the user if needed (optional, based on requirements)
        if (postRequest.getUser() != null) {
            post.setUser(UserDTOMapper.fromDTOtoUser(postRequest.getUser()));
        }

        // Save the updated post to the repository
        return postRepository.save(post);
    }

    @Override
    public boolean delete(int postId, int userId){
        int rowAffected = this.postRepository.deleteByIdAndUserId(postId, userId);
        return rowAffected > 0;
    }




}
