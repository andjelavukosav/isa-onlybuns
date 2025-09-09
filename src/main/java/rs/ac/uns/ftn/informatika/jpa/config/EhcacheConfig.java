package rs.ac.uns.ftn.informatika.jpa.config;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.*;
import javax.cache.spi.CachingProvider;

@Configuration
@EnableCaching
public class EhcacheConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EhcacheConfig.class);

    @PostConstruct
    public void registerCacheListeners() {
        try {
            CachingProvider provider = Caching.getCachingProvider();
            CacheManager cacheManager = provider.getCacheManager(); // koristi default config iz resources/ehcache.xml

            javax.cache.Cache<String, byte[]> postImagesCache = cacheManager.getCache("postImages", String.class, byte[].class);
            if (postImagesCache != null) {
                postImagesCache.registerCacheEntryListener(
                        new javax.cache.configuration.MutableCacheEntryListenerConfiguration<>(
                                () -> new PostImagesCacheListener(),
                                null,
                                true,
                                true
                        )
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error registering cache listener", e);
        }
    }

    public static class PostImagesCacheListener
            implements CacheEntryUpdatedListener<String, byte[]>,
            CacheEntryCreatedListener<String, byte[]>,
            CacheEntryRemovedListener<String, byte[]>
    {
        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends byte[]>> events) {
            for (CacheEntryEvent<? extends String, ? extends byte[]> event : events) {
                byte[] imageBytes = event.getValue();
                int size = (imageBytes != null) ? imageBytes.length : 0;
                LOG.info("Cache CREATED: key={} | size={} bytes", event.getKey(), size);
            }
        }

        @Override
        public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends byte[]>> events) {
            for (CacheEntryEvent<? extends String, ? extends byte[]> event : events) {
                LOG.info("Cache REMOVED: key={}", event.getKey());
            }
        }

        @Override
        public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends byte[]>> events) throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends String, ? extends byte[]> event : events) {
                byte[] imageBytes = event.getValue();
                int size = (imageBytes != null) ? imageBytes.length : 0;
                LOG.info("Cache UPDATED: key={} | size={} bytes", event.getKey(), size);
            }
        }
    }

}
