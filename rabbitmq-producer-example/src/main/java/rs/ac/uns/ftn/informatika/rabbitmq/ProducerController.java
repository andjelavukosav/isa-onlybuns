package rs.ac.uns.ftn.informatika.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.rabbitmq.manualWebSocket.ManualWebSocketClient;
import rs.ac.uns.ftn.informatika.rabbitmq.model.AsylumAndVeterinarian;

import java.net.URI;

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

	@PostMapping("/send-ws")
	public ResponseEntity<String> sendViaWebSocket(@RequestBody AsylumAndVeterinarian data) throws Exception {
		URI uri = new URI("ws://localhost:8080/ws/locations");
		ManualWebSocketClient client = new ManualWebSocketClient(uri);
		boolean connected = client.connectBlocking();
		if (!connected) {
			throw new IllegalStateException("WebSocket connection could not be established");
		}

		ObjectMapper mapper = new ObjectMapper();
		String message = mapper.writeValueAsString(data);
		client.send(message);
		client.close();
		return ResponseEntity.ok("Poruka poslata preko WebSocket-a");
	}


}
