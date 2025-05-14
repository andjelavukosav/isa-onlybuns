package rs.ac.uns.ftn.informatika.jpa.dto;

public class UserFollowStateDTO {
    private int followersCount;
    private int followingCount;

    public UserFollowStateDTO() {}

    public UserFollowStateDTO(int followersCount, int followingCount) {
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    public int getFollowersCount() { return followersCount; }

    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public int getFollowingCount() { return followingCount; }

    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

}
