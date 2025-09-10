package rs.ac.uns.ftn.informatika.jpa.bloomFilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class BloomFilterConfig {

    @Bean
    public BloomFilter<String> usernameBloomFilter() {
        return BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                10000, // očekivani broj korisnika
                0.01   // maksimalna greška 1%
        );
    }
}