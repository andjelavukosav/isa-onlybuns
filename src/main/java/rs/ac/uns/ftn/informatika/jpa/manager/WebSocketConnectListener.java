package rs.ac.uns.ftn.informatika.jpa.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketConnectListener {

    @Autowired
    private ChatListenerService chatListenerService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        if (event.getUser() == null) return;
        String username = event.getUser().getName();
        chatListenerService.startListenerForUser(username);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() == null) return;
        String username = event.getUser().getName();
        chatListenerService.stopListenerForUser(username);
    }
}
