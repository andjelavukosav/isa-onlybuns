package rs.ac.uns.ftn.informatika.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rabbitmq.model.AsylumAndVeterinarian;

@Component
public class Producer {
	
	private static final Logger log = LoggerFactory.getLogger(Producer.class);


	@Autowired
	private RabbitTemplate rabbitTemplate;

	
	/*
	 * Poruka se salje u exchange ciji je naziv prosledjen kao prvi parametar i
	 * exchange ce rutirati poruke u pravi queue.
	 */

	public void sendToExchange(String exchange, String routingKey, AsylumAndVeterinarian asylumAndVeterinarian){
		log.info("Sending> Message=[ {} ] to Exchange=[ {} ] with RoutingKey=[ {} ]", asylumAndVeterinarian, exchange, routingKey);
		this.rabbitTemplate.convertAndSend(exchange, routingKey, asylumAndVeterinarian);
	}
}
