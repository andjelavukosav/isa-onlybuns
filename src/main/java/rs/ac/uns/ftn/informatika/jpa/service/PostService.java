package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.dto.CreatePostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResults.PagedResults;

import java.util.List;

public interface PostService {
    PagedResults<PostDTO> findAll();
    PostDTO getPostById(Integer id);
    long getPostCountForUser(int userId);
    Post findById(int id);
    PostDTO update(CreatePostDTO updatePostRequest, int postId);
    boolean delete(int postId, int userId);
    PagedResults<PostDTO> findByUser(int userId);

    @Cacheable(value = "allPostsLastMonth", key = "'allPostsLastMonth'")
    List<Post> getAllPostsLastMonth();

    @Cacheable(value = "allPosts", key = "'allPosts'")
    List<Post> findAllPosts();

    @Cacheable(value = "top10PopularPosts", key = "'top10PopularPosts'")
    List<Post> getTop10PostsMostPopular();

    @Cacheable(value = "top5LikedPosts", key = "'top5LikedPosts'")
    List<Post> getAllPostsMostPopularLast7Days();
    //void likePost(int postId, int userId);


    @CacheEvict(value = {"allPostsLastMonth", "allPosts", "top5LikedPosts", "top10PopularPosts"}, allEntries = true)
    void removeFromCache();

    @CacheEvict(value = {"allPostsLastMonth", "allPosts"}, allEntries = true)
    void clearCache();

    List<Post> findNearbyPosts(double latitude, double longitude, double radius);

    PostDTO createPost(CreatePostDTO postRequest, int userId);

    String saveImage(MultipartFile file);

    boolean likePost(int postId, int userId);

    boolean unlikePost(int postId, int userId);

    List<UserDTO> getLikesFromPost(int postId);

    void updateLeaderboard(Post post);

}
