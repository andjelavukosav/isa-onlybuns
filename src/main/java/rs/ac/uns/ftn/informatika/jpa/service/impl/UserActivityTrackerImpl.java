package rs.ac.uns.ftn.informatika.jpa.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.jpa.service.UserActivityTracker;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserActivityTrackerImpl implements UserActivityTracker {

    private final Map<String, Instant> lastActivityMap = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public UserActivityTrackerImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Registrujemo custom metriku
        meterRegistry.gauge("active_users", this, tracker ->
                tracker.countActiveUsersLastMinutes(5)
        );
    }

    public void updateActivity(String username) {
        lastActivityMap.put(username, Instant.now());
    }

    public int countActiveUsersLastMinutes(int minutes) {
        Instant now = Instant.now();
        return (int) lastActivityMap.values().stream()
                .filter(time -> time.isAfter(now.minusSeconds(minutes * 60L)))
                .count();
    }
}
