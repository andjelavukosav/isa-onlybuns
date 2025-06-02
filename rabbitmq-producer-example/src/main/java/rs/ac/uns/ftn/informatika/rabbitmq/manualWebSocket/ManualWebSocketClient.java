package rs.ac.uns.ftn.informatika.rabbitmq.manualWebSocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;


public class ManualWebSocketClient extends WebSocketClient {

    public ManualWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket opened");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}

