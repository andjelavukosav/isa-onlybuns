package rs.ac.uns.ftn.informatika.jpa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Entity
@Table(name = "Users")
public class User implements UserDetails {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String username;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "last_password_reset_date")
    private Timestamp lastPasswordResetDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles;

    @ManyToOne(fetch = FetchType.EAGER)  // One-to-many from Address to User
    @JoinColumn(name = "address_id")
    private Address address;  // A user can have one address

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Post> posts = new HashSet<Post>();

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Follow> followers = new HashSet<>();

    @Column(name="following_count", nullable = false, columnDefinition = "int default 0")
    private int followingCount = 0;

    @Column(name="followers_count", nullable = false, columnDefinition = "int default 0")
    private int followersCount = 0;

    @Column(name="posts_count", nullable = false, columnDefinition = "int default 0")
    private int postsCount = 0;

    @Version
    private Integer version;
    @PrePersist
    public void setVersionToZeroIfNull() {
        if (version == null) {
            version = 0; // Postavljanje verzije na 0 pre nego što se entitet sačuva
        }
    }

    public User() {super();}

    public User(int id, String username, String password, String firstName, String lastName, String email) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    public User(int id, String username, String password, String firstName, String lastName, String email, int followingCount, int followersCount, int postsCount) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.followingCount = followingCount;
        this.followersCount = followersCount;
        this.postsCount = postsCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        Timestamp now = new Timestamp(new Date().getTime());
        this.setLastPasswordResetDate(now);
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Role> getRoles() {
        return roles;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Timestamp getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Timestamp lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    public void setPosts(Set<Post> posts) {
        this.posts = posts;
    }

    public void addPost(Post post) {
        this.posts.add(post);
        post.setUser(this);
        this.incrementPostsCount();
    }

    public  void removePost(Post post) {
        this.posts.remove(post);
        post.setUser(null);
        this.decrementPostsCount();
    }

    public int getPostsCount() { return this.postsCount; }

    public void setPostsCount(int postsCount) { this.postsCount = postsCount; }

    public int getFollowingCount() { return this.followingCount; }

    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getFollowersCount() { return this.followersCount; }

    public void setFollowersCount(int followerCount) { this.followersCount = followerCount; }

    public Integer getVersion() { return version; }

    public void setVersion(Integer version) { this.version = version; }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public Set<Follow> getFollowers() { return followers; }

    public void setFollowers(Set<Follow> followers) { this.followers = followers; }

    public Set<Follow> getFollowing() { return following; }

    public void setFollowing(Set<Follow> following) { this.following = following; }

    public void addFollowing(Follow follow) {
        this.following.add(follow);
        follow.setFollower(this);
        incrementFollowingCount();
    }
    public void removeFollowing(Follow follow) {
        this.following.remove(follow);
        follow.setFollower(null);
        this.decrementFollowingCount();
        System.out.println("Updated following count: " + this.followingCount);
    }
    public void addFollower(Follow follow) {
        this.followers.add(follow);
        follow.setFollowed(this);
        incrementFollowersCount();
    }
    public void removeFollower(Follow follow) {
        this.followers.remove(follow);
        follow.setFollowed(null);
        this.decrementFollowersCount();
    }

    private void incrementFollowersCount() { this.followersCount++; }

    private void decrementFollowersCount(){
        if(this.followersCount>0){
            this.followersCount--;
        }
    }

    private void incrementFollowingCount() { this.followingCount++; }

    private void decrementFollowingCount(){
        if(this.followingCount>0){
            this.followingCount--;
        }
    }

    private void incrementPostsCount() { this.postsCount++; }

    private void decrementPostsCount(){
        if(this.postsCount>0){
            this.postsCount--;
        }
    }


}
