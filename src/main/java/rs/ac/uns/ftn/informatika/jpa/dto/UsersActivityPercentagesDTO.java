package rs.ac.uns.ftn.informatika.jpa.dto;

public class UsersActivityPercentagesDTO {
    private double postsPercentage;
    private double commentsOnlyPercentage;
    private double inactivePercentage;

    public UsersActivityPercentagesDTO(double postsPercentage, double commentsOnlyPercentage, double inactivePercentage) {
        this.postsPercentage = postsPercentage;
        this.commentsOnlyPercentage = commentsOnlyPercentage;
        this.inactivePercentage = inactivePercentage;
    }

    public double getPostsPercentage() { return postsPercentage; }
    public void setPostsPercentage(double postsPercentage) { this.postsPercentage = postsPercentage; }

    public double getCommentsOnlyPercentage() { return commentsOnlyPercentage; }
    public void setCommentsOnlyPercentage(double commentsOnlyPercentage) { this.commentsOnlyPercentage = commentsOnlyPercentage; }

    public double getInactivePercentage() { return inactivePercentage; }
    public void setInactivePercentage(double inactivePercentage) { this.inactivePercentage = inactivePercentage; }
}
