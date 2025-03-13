package rs.ac.uns.ftn.informatika.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.rabbitmq.model.AsylumAndVeterinarian;

@RestController
@RequestMapping(value = "api")
public class ProducerController {

	@Autowired
	private Producer producer;


	@PostMapping(value="/{exchange}/{queue}", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> sendMessageToExchange(
			@PathVariable("exchange") String exchange,
			@PathVariable("queue") String queue,
			@RequestBody AsylumAndVeterinarian asylumAndVeterinarian) {

		producer.sendToExchange(exchange, queue, asylumAndVeterinarian);
		return ResponseEntity.ok("Identifikator,ime i lokacija  poslate u MQ!");
	}

}
