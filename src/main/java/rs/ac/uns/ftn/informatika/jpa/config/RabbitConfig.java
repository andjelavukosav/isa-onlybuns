package rs.ac.uns.ftn.informatika.jpa.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableRabbit
public class RabbitConfig {
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();  // Koristi JSON message converter
    }

    @Bean
    public FanoutExchange adsExchange(@Value("${rabbitmq.exchange}") String exchangeName) {
        return new FanoutExchange(exchangeName);
    }

    @Bean
    public Queue myQueue() {
        return new Queue("spring-boot1", true); // true = durable
    }

}
