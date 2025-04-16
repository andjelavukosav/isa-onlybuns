package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.UserFollowStateDTO;

public interface FollowService {
    UserFollowStateDTO followUser(int followerId, int followedId);
    UserFollowStateDTO unfollowUser(int followerId, int followedId);
    boolean isFollowing(int followerId, int followedId);
}
