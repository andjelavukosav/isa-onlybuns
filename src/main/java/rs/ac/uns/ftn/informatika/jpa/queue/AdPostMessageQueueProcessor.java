package rs.ac.uns.ftn.informatika.jpa.queue;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import rs.ac.uns.ftn.informatika.jpa.dto.AdPostMessageDTO;

import javax.annotation.PostConstruct;

@Component
public class AdPostMessageQueueProcessor {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String advertisingAppUrl = "http://localhost:8081/api/ads/receive"; // URL reklamne aplikacije

    @PostConstruct
    @Async
    public void processQueue() {
        new Thread(() -> {
            while (true) {
                try {
                    AdPostMessageDTO message = AdPostMessageQueue.takeMessage();
                    System.out.println("📤 Šaljem reklamu aplikaciji za reklamiranje: " + message.getDescription());

                    restTemplate.postForObject(advertisingAppUrl, message, Void.class);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
