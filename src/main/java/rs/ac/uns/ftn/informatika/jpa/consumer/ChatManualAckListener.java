package rs.ac.uns.ftn.informatika.jpa.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatMessageDTO;

public class ChatManualAckListener implements ChannelAwareMessageListener {

    private final String username;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final MessageConverter messageConverter;

    public ChatManualAckListener(String username, SimpMessagingTemplate messagingTemplate,
                                 SimpUserRegistry simpUserRegistry, MessageConverter messageConverter)
    {
        this.username = username;
        this.messagingTemplate = messagingTemplate;
        this.simpUserRegistry = simpUserRegistry;
        this.messageConverter = messageConverter;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        ChatMessageDTO chatMessage = (ChatMessageDTO) messageConverter.fromMessage(message);

        boolean online = simpUserRegistry.getUser(username) != null;

        if (online) {
            System.out.println("Korisnik " + username + " je online");
            System.out.println(" Primljena poruka za " + username + ": " + chatMessage.getMessage());
            Thread.sleep(300); // da frontend stigne da SUBSCRIBE

            messagingTemplate.convertAndSendToUser(username, "/queue/messages", chatMessage);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            System.out.println("Korisnik " + username + " je offline");
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}