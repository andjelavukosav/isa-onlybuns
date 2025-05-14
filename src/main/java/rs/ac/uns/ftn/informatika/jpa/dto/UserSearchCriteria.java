package rs.ac.uns.ftn.informatika.jpa.dto;

public class UserSearchCriteria {
    private String firstName;
    private String lastName;
    private String email;
    private Integer minPostsCount;
    private Integer maxPostsCount;

    public UserSearchCriteria() {}

    public UserSearchCriteria(String firstName, String lastName, String email, Integer minPostsCount, Integer maxPostsCount) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.minPostsCount = minPostsCount;
        this.maxPostsCount = maxPostsCount;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getMinPostsCount() { return minPostsCount; }
    public void setMinPostsCount(Integer minPostsCount) { this.minPostsCount = minPostsCount; }

    public Integer getMaxPostsCount() { return maxPostsCount; }
    public void setMaxPostsCount(Integer maxPostsCount) { this.maxPostsCount = maxPostsCount; }

    public boolean isEmpty() {
        return (firstName == null || firstName.isEmpty()) &&
                (lastName == null || lastName.isEmpty()) &&
                (email == null || email.isEmpty()) &&
                (minPostsCount == null) &&
                (maxPostsCount == null);
    }

}
