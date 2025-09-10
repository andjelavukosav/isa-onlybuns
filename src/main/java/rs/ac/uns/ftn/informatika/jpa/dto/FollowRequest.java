package rs.ac.uns.ftn.informatika.jpa.dto;

public class FollowRequest {
    private int followerId;
    private int followedId;

    public FollowRequest() {}
    public FollowRequest(int followerId, int followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }
    public int getFollowerId() { return followerId; }

    public void setFollowerId(int followerId) { this.followerId = followerId; }

    public int getFollowedId() { return followedId; }

    public void setFollowedId(int followedId) { this.followedId = followedId; }
}