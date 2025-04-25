package rs.ac.uns.ftn.informatika.jpa.service.impl;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.UserFollowStateDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Follow;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.FollowRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.FollowService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Transactional
    @RateLimiter(name="standard", fallbackMethod = "fallback")
    //@Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 500, maxDelay = 2000, multiplier = 1.5))
    public UserFollowStateDTO followUser(int followerId, int followedId) {
        User follower = userService.findById(followerId);
        User followed = userService.findById(followedId);

        if(followRepository.existsByFollowerAndFollowed(follower, followed)) {
            throw new IllegalStateException("User is already following this person.");
        }
        Follow follow = new Follow(follower, followed);

        follower.addFollowing(follow);
        followed.addFollower(follow);

        followRepository.save(follow);

        return new UserFollowStateDTO(followed.getFollowersCount(), followed.getFollowingCount());
    }

    @Transactional
    public UserFollowStateDTO unfollowUser(int followerId, int followedId) {
        User follower = userService.findById(followerId);
        User followed = userService.findById(followedId);

        Follow follow = followRepository.findByFollowerAndFollowed(follower, followed);
        if(follow == null){
            throw new IllegalStateException("Follow not found. User is not following this person.");
        }

        follower.removeFollowing(follow);
        followed.removeFollower(follow);

        return new UserFollowStateDTO(followed.getFollowersCount(), followed.getFollowingCount());
    }

    @Transactional
    public boolean isFollowing(int followerId, int followedId) {
        User follower = userService.findById(followerId);
        User followed = userService.findById(followedId);

        return followRepository.existsByFollowerAndFollowed(follower, followed);
    }

    private UserFollowStateDTO fallback(int followerId, int followedId, RequestNotPermitted ex) {
        logger.warn("You have exceeded the limit of follows per minute. Please try again later.");
        throw ex;
    }

}
