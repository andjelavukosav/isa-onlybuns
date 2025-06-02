package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.dto.CreatePostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UserDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.PostDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.pagedResults.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
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
    private PostDTOMapper postDTOMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CacheManager cacheManager;

    private final Logger LOG = LoggerFactory.getLogger(PostServiceImpl.class);

    // PriorityQueue za top 10 najlajkovanijih objava svih vremena
    private final PriorityQueue<Post> top10LikedPosts = new PriorityQueue<>(
            Comparator.comparingLong(Post::getLikeCount) // Sortira rastuće po broju lajkova
    );


    //PriorityQueue za održavanje TOP 5 postova u memoriji
    public final PriorityQueue<Post> leaderboard = new PriorityQueue<>(
            Comparator.comparingLong(this::getPostLikeCount) // Sortira po broju lajkova
    );

    @PostConstruct
    @Transactional
    public void initializeTop10LikedPosts() {
        List<Post> topPosts = postRepository.findTop10ByMostLikedAllTime();

        synchronized (top10LikedPosts) {
            top10LikedPosts.clear();

            for (Post post : topPosts) {
                if (post.getLikeCount() == 0) {
                    continue; // Preskoči postove bez lajkova
                }

                top10LikedPosts.offer(post);

                if (top10LikedPosts.size() > 10) {
                    top10LikedPosts.poll(); // Ukloni višak ako pređe 10 elemenata
                }
            }
        }

        LOG.info("Top 10 most liked posts initialized. Size: {}", top10LikedPosts.size());
    }


    @PostConstruct
    @Transactional
    public void initializeLeaderboard() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Post> topPosts = postRepository.findTop5ByLikesInLast7Days(sevenDaysAgo);

        if (topPosts.isEmpty()) {
            LOG.info("No posts with likes in the last 7 days. Leaderboard remains empty.");
            return;
        }

        synchronized (leaderboard) {
            leaderboard.clear();

            for (Post post : topPosts) {
                leaderboard.offer(post);

                if (leaderboard.size() > 5) {
                    leaderboard.poll(); // Ukloni višak ako pređe 5
                }
            }

            LOG.info("Leaderboard initialized with {} posts.", leaderboard.size());
        }
    }



    // ✅ Broj lajkova za post
    @Transactional
    public long getPostLikeCount(Post post) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return post.getLikes().stream()
                .filter(like -> like.getCreationDateTime().isAfter(sevenDaysAgo))
                .count();
    }

    @Override
    public void updateLeaderboard(Post post) {

        synchronized (leaderboard) {
            leaderboard.removeIf(p -> p.getId() == post.getId());
            leaderboard.offer(post);

            if (leaderboard.size() > 5) {
                leaderboard.poll();
            }

            cacheManager.getCache("top5LikedPosts").put("top5LikedPosts", new ArrayList<>(leaderboard));
        }

        synchronized (top10LikedPosts) {
            top10LikedPosts.removeIf(p -> p.getId() == post.getId());
            top10LikedPosts.offer(post);

            if (top10LikedPosts.size() > 10) {
                top10LikedPosts.poll();
            }

            cacheManager.getCache("top10PopularPosts").put("top10PopularPosts", new ArrayList<>(top10LikedPosts));
        }

        LOG.info("Post {} updated in leaderboard and top 10 most liked posts.", post.getId());

    }



    // ✅ Dohvatanje top 5 postova iz keša
    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPostsMostPopularLast7Days() {
        synchronized (leaderboard) {
            return new ArrayList<>(leaderboard);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResults<PostDTO> findAll() {
        List<PostDTO> posts = postRepository.findAll().stream()
                .sorted(Comparator.comparing(Post::getCreationDateTime).reversed())
                .map(PostDTO::new)
                .collect(Collectors.toList());

        return new PagedResults<>(posts, posts.size());
    }


    @Override
    @Transactional
    public PostDTO createPost(CreatePostDTO postRequest, int userId) {

        String imagePath = this.saveImage(postRequest.getImage());

        User user = userService.findById(userId);

        Post newPost = new Post(postRequest.getDescription(), imagePath, LocalDateTime.now(), postRequest.getLatitude(), postRequest.getLongitude());

        user.addPost(newPost);

        return postDTOMapper.fromPostToDTO(newPost);
    }

    public String saveImage(MultipartFile imageFile) {
        try{
            // Definišite folder za čuvanje slika unutar statičkog direktorijuma
            String uploadDir = "uploads/images";
            Path uploadPath = Paths.get(uploadDir);

            // Kreirajte folder ako ne postoji
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generišite jedinstveno ime za fajl
            String fileName = UUID.randomUUID().toString() + "-" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Sačuvajte fajl u folder
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Vratite ime fajla koje će se koristiti za pristup slici
            return "/images/" + fileName;
        }catch(IOException e){
            throw new RuntimeException("An error occurred while saving the image, ", e);
        }
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

    @Override
    @Transactional
    public PostDTO update(CreatePostDTO updatePostRequest, int postId) {

        Post post = this.findById(postId);

        if(updatePostRequest.getDescription() != null){
            post.setDescription(updatePostRequest.getDescription());
        }
        if(updatePostRequest.getImage() != null){
            String newImagePath = this.saveImage(updatePostRequest.getImage());
            post.setImagePath(newImagePath);
        }
        if(updatePostRequest.getLatitude()!= null && updatePostRequest.getLongitude() != null){
            post.setLocation(new Location(updatePostRequest.getLatitude(), updatePostRequest.getLongitude()));
        }

        post.setCreationDateTime(LocalDateTime.now());

        return postDTOMapper.fromPostToDTO(post);

    }

    @Override
    @Transactional
    public boolean delete(int postId, int userId) {
        User user = userService.findById(userId);
        Post postToDelete = postRepository.findById(postId);
        if(postToDelete != null){
            user.removePost(postToDelete);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public PagedResults<PostDTO> findByUser(int userId) {
        List<PostDTO> posts =  postRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(Post::getCreationDateTime).reversed())
                .map(post -> {
                    boolean isLiked = likeService.findLikeByPostIdAndUserId(post.getId(), userId) != null ;

                    PostDTO postDTO = new PostDTO(post);
                    postDTO.isLikedByCurrentUser = isLiked;
                    return postDTO;
                })
                .collect(Collectors.toList());

        return new PagedResults<>(posts, posts.size());
    }


    public List<Post> getAllPostsLastMonth() {
        List<Post> posts = this.postRepository.findAll();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1L);
        this.LOG.info("Posts(last month) successfully cached!");
        return (List)posts.stream().filter((post) -> {
            return post.getCreationDateTime().isAfter(oneMonthAgo);
        }).collect(Collectors.toList());
    }

    public List<Post> findAllPosts() {
        this.LOG.info("Posts(all posts) successfully cached!");
        return this.postRepository.findAll();
    }

    public void removeFromCache() {
        this.LOG.info("All posts(all, last month, top5, top10) removed from cache!");
    }

    public void clearCache() {
        this.LOG.info("All posts and posts last month removed from cache!");
    }

    @Transactional
    public List<Post> getTop10PostsMostPopular() {
        synchronized(this.top10LikedPosts) {
            return new ArrayList(this.top10LikedPosts);
        }
    }

    public List<Post> findNearbyPosts(double latitude, double longitude, double radius) {
        return this.postRepository.findNearbyPosts(latitude, longitude, radius);
    }

    @Override
    @Transactional
    public boolean likePost(int postId, int userId) {

        if(likeService.findLikeByPostIdAndUserId(postId, userId) != null){
            return false;
        }

        Post post = postRepository.findById(postId);
        User user = userService.findById(userId);


        Like like = new Like(user, post);
        like.setCreationDateTime(LocalDateTime.now());

        user.addLike(like);
        post.likePost(like);

        this.updateLeaderboard(post);
        return true;

    }

    @Override
    @Transactional
    public boolean unlikePost(int postId, int userId){

        Like like = likeService.findLikeByPostIdAndUserId(postId, userId);

        if (like != null) {
            Post post = like.getPost();
            post.unlikePost(like);
            like.getUser().removeLike(like);

            this.updateLeaderboard(post);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public List<UserDTO> getLikesFromPost(int postId) {
        Set<Like> likes = findById(postId).getLikes();

        if(likes == null){
            return Collections.emptyList();
        }

        List<User> users = likes.stream().map(Like::getUser).collect(Collectors.toList());

        return userDTOMapper.toUserDTOList(users);

    }


}
