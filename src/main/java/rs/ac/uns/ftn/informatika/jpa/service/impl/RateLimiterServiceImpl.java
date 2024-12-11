package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.service.RateLimiterService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {
    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimiterServiceImpl() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5) // Maksimalan broj pokušaja
                .limitRefreshPeriod(Duration.ofMinutes(1)) // Period resetovanja
                .timeoutDuration(Duration.ZERO) // Nema čekanja
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(config);
    }

    public RateLimiter getRateLimiter(String ipAddress) {
        return rateLimiterRegistry.rateLimiter(ipAddress);
    }
}
