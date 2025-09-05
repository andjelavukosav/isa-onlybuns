package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.AdPostMessageDTO;
import rs.ac.uns.ftn.informatika.jpa.service.AdMessageSender;

@Service
public class AdMessageSenderImpl implements AdMessageSender {
    private final RabbitTemplate rabbitTemplate;
    private final FanoutExchange fanoutExchange;

    public AdMessageSenderImpl(RabbitTemplate rabbitTemplate, FanoutExchange fanoutExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.fanoutExchange = fanoutExchange;
    }

    public void sendAdPost(AdPostMessageDTO message) {
        rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", message);
    }
}
