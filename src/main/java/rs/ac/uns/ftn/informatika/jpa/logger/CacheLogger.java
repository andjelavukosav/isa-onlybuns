package rs.ac.uns.ftn.informatika.jpa.logger;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheLogger implements CacheEventListener<Object, Object> {
    private final Logger LOG = LoggerFactory.getLogger(CacheLogger.class);

    @Override
    public void onEvent(CacheEvent<?, ?> cacheEvent) {
        Object newVal = cacheEvent.getNewValue();
        Object oldVal = cacheEvent.getOldValue();

        // ako je ovo keš sa slikama
        if (newVal instanceof byte[] || oldVal instanceof byte[]) {
            int newSize = (newVal instanceof byte[]) ? ((byte[]) newVal).length : 0;
            int oldSize = (oldVal instanceof byte[]) ? ((byte[]) oldVal).length : 0;
            LOG.info("Key: {} | EventType: {} | Old size={} bytes | New size={} bytes",
                    cacheEvent.getKey(), cacheEvent.getType(), oldSize, newSize);
            return;
        }

        // za ostale keševe
        LOG.info("Key: {} | EventType: {} | Old value: {} | New value: {}",
                cacheEvent.getKey(), cacheEvent.getType(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}
