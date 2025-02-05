package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;

import org.springframework.cache.annotation.Cacheable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDTOMapper userDTOMapper;

    @Autowired
    private LikeService likeService;

    private final Logger LOG = LoggerFactory.getLogger(PostServiceImpl.class);


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

    @Override
    public List<Post> findByUserId(int userId) throws AccessDeniedException {
        return postRepository.findByUserId(userId);
    }

    @Override
    public List<Post> getAllPostsLastMonth() {
        List<Post> posts = postRepository.findAll();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        LOG.info("Posts(last month) successfully cached!");
        return posts.stream()
                .filter(post -> post.getCreationDateTime().isAfter(oneMonthAgo))
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> findAllPosts(){
        LOG.info("Posts(all posts) successfully cached!");
        return postRepository.findAll();
    }

   /* @Override
    public Post findOne(int id) {
        LOG.info("Post with id: " + id + " successfully cached!");
        return this.postRepository.findById(id);
    }*/

    @Override
    public void removeFromCache() {
        LOG.info("Products removed from cache!");

    }

    @Override
    public List<Post> getTop10PostsMostPopular() {
        List<Post> posts = postRepository.findAll();

        // Mapiraćemo Post objekte na broj lajkova
        Map<Post, Long> postLikeCountMap = new HashMap<>();

        // Iteriraj kroz sve postove i broj lajkove
        for (Post post : posts) {
            // Dobavi lajkove za trenutni post
            List<LikeDTO> likes = likeService.findLikesByPostId(post.getId());

            // Dobavi ukupan broj lajkova za trenutni post
            long likeCount = likes.size();  // Ukupan broj lajkova za post

            // Dodaj broj lajkova u mapu
            postLikeCountMap.put(post, likeCount);
        }

        LOG.info("Posts(top 10) successfully cached!");
        // Sortiraj postove po broju lajkova (od najviše ka najmanje) i uzmi top 10
        return postLikeCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))  // Sortiraj opadajuće
                .map(Map.Entry::getKey)  // Uzmi samo postove
                .limit(10)  // Top 10
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getAllPostsMostPopularLast7Days() {
        List<Post> posts = postRepository.findAll();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        Map<Post, Long> postLikeCountMap = new HashMap<>();

        for (Post post : posts) {
            // Dobavi lajkove za trenutni post
            List<LikeDTO> likes = likeService.findLikesByPostId(post.getId());

            // Filtriraj lajkove koji su postavljeni u poslednjih 7 dana
            long likeCountLast7Days = likes.stream()
                    .filter(like -> like.getCreationDateTime().isAfter(sevenDaysAgo))
                    .count();

            postLikeCountMap.put(post, likeCountLast7Days);
        }

        LOG.info("Posts(top 5) successfully cached!");
        // Sortiraj postove po broju lajkova i uzmi top 5
        return postLikeCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }
}
