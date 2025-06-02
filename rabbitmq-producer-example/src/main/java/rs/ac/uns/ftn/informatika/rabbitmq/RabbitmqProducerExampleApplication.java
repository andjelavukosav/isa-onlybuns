package rs.ac.uns.ftn.informatika.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/*
 * 
 * Za pokretanje primera potrebno je instalirati RabbitMQ - https://www.rabbitmq.com/download.html
 */
@SpringBootApplication
public class RabbitmqProducerExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqProducerExampleApplication.class, args);
	}

	@Value("${myqueue}")
	String queue;

	@Value("${myexchange}")
	String exchange;
	
	@Value("${routingkey}")
	String routingkey;


	//Kako za queue nije definisan exchange, on se vezuje za Default Exchange, 
	//gde je njegov naziv zapravo i Routing Key.
	@Bean
	Queue queue() {
		return new Queue(queue, true);
	}


	//Ovim se queue2 vezuje za exchange (parametar to() metode je naziv)
	//pod definisanim ključem (parametar with() metode je ključ, odnosno routing key)
	@Bean
	DirectExchange exchange() {
		return new DirectExchange(exchange);
	}

	@Bean
	Binding binding(Queue queue2, DirectExchange exchange) {
		return BindingBuilder.bind(queue2).to(exchange).with(routingkey);
	}

	/*
	 * Registrujemo bean koji ce sluziti za konekciju na RabbitMQ gde se mi u
	 * primeru kacimo u lokalu.
	 */

	//uspostavljanje konekcije sa MQ serverom
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
		return connectionFactory;
	}

}
