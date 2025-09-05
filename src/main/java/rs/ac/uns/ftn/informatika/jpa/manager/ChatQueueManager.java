package rs.ac.uns.ftn.informatika.jpa.manager;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatQueueManager {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private TopicExchange chatExchange;

    public void createQueueForUser(String username) {
        String queueName = "chat.queue." + username;
        String routingKey = "chat." + username;

        Queue queue = new Queue(queueName, true); // durable
        amqpAdmin.declareQueue(queue);
        System.out.println("Created queue: " + queue.getName());
        Binding binding = BindingBuilder.bind(queue).to(chatExchange).with(routingKey);
        amqpAdmin.declareBinding(binding);
    }
}
