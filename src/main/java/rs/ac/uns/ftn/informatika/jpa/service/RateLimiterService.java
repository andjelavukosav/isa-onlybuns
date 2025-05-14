package rs.ac.uns.ftn.informatika.jpa.service;

import io.github.resilience4j.ratelimiter.RateLimiter;

public interface RateLimiterService {
    RateLimiter getRateLimiter(String ipAddress);
}
