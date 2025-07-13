package rs.ac.uns.ftn.informatika.jpa.util;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoggedUserTracker {

    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, Date> loggedOutTimestamps = new ConcurrentHashMap<>();

    public void userLoggedIn(String email) {
        activeUsers.add(email);
        loggedOutTimestamps.remove(email);
    }

    public void userLoggedOut(String email) {
        activeUsers.remove(email);
        loggedOutTimestamps.put(email, new Date());
        System.out.println("User logged out: " + email);
    }

    public boolean isUserLoggedIn(String email) {
        return activeUsers.contains(email);
    }

    public Set<String> getLoggedInUsers() {
        return activeUsers;
    }

    public Map<String, Date> getLoggedOutTimestamps() {
        return loggedOutTimestamps;
    }

    public Set<String> getUsersLoggedOutLongerThan(long millis) {
        Date now = new Date();
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, Date> entry : loggedOutTimestamps.entrySet()) {
            long diff = now.getTime() - entry.getValue().getTime();
            if (diff >= millis) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
