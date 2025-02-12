package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.PostDTO;
import rs.ac.uns.ftn.informatika.jpa.mapper.UserDTOMapper;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;

import javax.annotation.PostConstruct;
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
    private LikeService likeService;

    @Autowired
    private CacheManager cacheManager;

    private final Logger LOG = LoggerFactory.getLogger(PostServiceImpl.class);

    // PriorityQueue za top 10 najlajkovanijih objava svih vremena
    private final PriorityQueue<Post> top10LikedPosts = new PriorityQueue<>(
            Comparator.comparingLong(this::getLikeCount) // Sortira rastuće po broju lajkova
    );


    //PriorityQueue za održavanje TOP 5 postova u memoriji
    private final PriorityQueue<Post> leaderboard = new PriorityQueue<>(
            Comparator.comparingLong(this::getPostLikeCount) // Sortira po broju lajkova
    );

    @PostConstruct
    public void initializeTop10LikedPosts() {
        List<Post> topPosts = postRepository.findTop10ByMostLikedAllTime();

        synchronized (top10LikedPosts) {
            top10LikedPosts.clear();

            for (Post post : topPosts) {
                top10LikedPosts.offer(post);

                if (top10LikedPosts.size() > 10) {
                    top10LikedPosts.poll(); // Ukloni višak ako pređe 10 elemenata
                }
            }
        }

        LOG.info("Top 10 most liked posts initialized. Size: {}", top10LikedPosts.size());
    }

    @PostConstruct
    public void initializeLeaderboard() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Post> topPosts = postRepository.findTop5ByLikesInLast7Days(sevenDaysAgo);

        if (topPosts.isEmpty()) {
            List<Post> recentPosts = postRepository.findTop5ByDateLast7Days(sevenDaysAgo);
            topPosts = recentPosts.stream().limit(5).collect(Collectors.toList()); // Ograniči na 5
        }

        synchronized (leaderboard) {
            leaderboard.clear();

            for (Post post : topPosts) {
                leaderboard.offer(post);

                if (leaderboard.size() > 5) {
                    leaderboard.poll(); // Ukloni višak ako pređe 5 elemenata
                }
            }


            LOG.info("Leaderboard initialized with {} posts.", topPosts.size());
        }

    }

    private long getLikeCount(Post post) {
        return likeService.countLikesByPostId(post.getId());
    }


    // ✅ Broj lajkova za post
    private long getPostLikeCount(Post post) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return likeService.findLikesByPostId(post.getId()).stream()
                .filter(like -> like.getCreationDateTime().isAfter(sevenDaysAgo))
                .count();
    }

    @Override
    public void likePost(int postId, int userId) {
        Post post = postRepository.findById(postId);

        if (post == null) {
            throw new RuntimeException("Post not found");
        }

        Like like = new Like();
        like.setCreationDateTime(LocalDateTime.now());
        like.setPost(post);
        like.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found")));

        likeService.save(like); // Spasi novi lajk

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
    public List<Post> getAllPostsMostPopularLast7Days() {
        synchronized (leaderboard) {
            return new ArrayList<>(leaderboard);
        }
    }

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
        LOG.info("All posts(all, last month, top5, top10) removed from cache!");
    }

    @Override
    public void clearCache(){
        LOG.info("All posts and posts last month removed from cache!");
    }

    @Override
    public List<Post> getTop10PostsMostPopular() {
        synchronized (top10LikedPosts) {
            return new ArrayList<>(top10LikedPosts);
        }
    }


 /*   @Override
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
    }*/

 /*   @Override
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
    }*/
}
