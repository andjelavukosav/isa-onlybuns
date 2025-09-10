package rs.ac.uns.ftn.informatika.jpa.dto;

import rs.ac.uns.ftn.informatika.jpa.model.User;

import java.util.Date;

public class UserDTO {
    private int id;

    private String username;

    private String password;

    private String firstname;

    private String lastname;

    private String email;

    private int followersCount = 0;

    private int followingCount = 0;

    private int postsCount = 0;

    private Date lastPasswordResetDate;

    public UserDTO() {

    }

    public UserDTO(User user) {
        this(user.getId(), user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getFollowingCount(), user.getFollowersCount(), user.getPostsCount());
    }

    public UserDTO(int id, String username, String password, String firstname, String lastname, String email, int followingCount, int followersCount, int postsCount) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.followingCount = followingCount;
        this.followersCount = followersCount;
        this.postsCount = postsCount;
    }

    public UserDTO(int id, String username, String password, String firstname, String lastname, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    private boolean enabled;

    private AddressDTO address;  // Add this field to represent the address

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }
    public int getFollowingCount() { return this.followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }
    public int getFollowersCount() {return this.followersCount;}

    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public int getPostsCount() { return this.postsCount; }

    public void setPostsCount(int postsCount) { this.postsCount = postsCount; }

    public Date getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Date lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

}
