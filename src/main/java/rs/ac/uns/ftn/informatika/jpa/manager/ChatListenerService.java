package rs.ac.uns.ftn.informatika.jpa.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.consumer.ChatManualAckListener;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatListenerService {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageConverter jackson2JsonMessageConverter;

    private Map<String, SimpleMessageListenerContainer> listenerContainers = new HashMap<>();
    @Autowired
    private ChatQueueManager chatQueueManager;

    public void startListenerForUser(String username) {
        if (listenerContainers.containsKey(username)) return;

        // Kreiraj red ako nije kreiran
        chatQueueManager.createQueueForUser(username);
        System.out.println("Kreiran je red za korisnika " + username);

        ChatManualAckListener listener = new ChatManualAckListener(username, messagingTemplate, simpUserRegistry, jackson2JsonMessageConverter);
        //MessageListenerAdapter adapter = new MessageListenerAdapter(listener, "onMessage");
        //adapter.setMessageConverter(jackson2JsonMessageConverter);
        System.out.println(" Pokrenut listener za korisnika: " + username);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("chat.queue." + username);
        container.setMessageListener(listener);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.start();

        listenerContainers.put(username, container);
    }

    public void stopListenerForUser(String username) {
        SimpleMessageListenerContainer container = listenerContainers.remove(username);
        if (container != null) {
            container.stop();
        }
    }
}
