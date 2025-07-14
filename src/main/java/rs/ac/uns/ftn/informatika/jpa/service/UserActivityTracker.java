package rs.ac.uns.ftn.informatika.jpa.service;

public interface UserActivityTracker {
    void updateActivity(String username);
    int countActiveUsersLastMinutes(int minutes);
}
