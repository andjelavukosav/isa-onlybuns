package rs.ac.uns.ftn.informatika.jpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class LikeConcurrencyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testConcurrentLikes() throws InterruptedException {
        int numberOfThreads = 10;
        int postId = 4;
        int baseUserId = 1;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int userId = baseUserId + i;
            createTestUserIfNotExists(userId);
            executor.execute(() -> {
                try {
                    Thread.sleep(100); // simulacija kasnjenja
                    postService.likePost(postId, userId);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // cekamo da sve niti zavrse

        entityManager.clear();
        Post post = postRepository.findById(postId);

        assertEquals(numberOfThreads, post.getLikeCount());
    }

    private void createTestUserIfNotExists(int userId) {
        if (!userRepository.existsById(userId)) {
            User user = new User();
            user.setId(userId);
            user.setUsername("testuser" + userId);
            user.setPassword("password");
            user.setEmail("test" + userId + "@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setEnabled(true);
            userRepository.save(user);
        }
    }
}