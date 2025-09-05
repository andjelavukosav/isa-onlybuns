package rs.ac.uns.ftn.informatika.jpa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import rs.ac.uns.ftn.informatika.jpa.security.auth.TokenBasedAuthentication;
import rs.ac.uns.ftn.informatika.jpa.util.TokenUtils;

import java.security.Principal;
import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private TokenUtils tokenUtils;

    @Qualifier("userDetailsService")
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // frontend se pretplaćuje ovde
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:4200")
                .withSockJS();
    }


   @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

               if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String jwtToken = accessor.getFirstNativeHeader("Authorization"); //jwt se salje u STOMP header-u "Authorization"
                    if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                        jwtToken = jwtToken.substring(7);


                        String email = tokenUtils.getUsernameFromToken(jwtToken);

                        if (email != null) { //web socket konekcija mora imati svoj kontekst autentifikacije korisnika koji se odrzava tokom cijele sesije
                            UserDetails user = userDetailsService.loadUserByUsername(email);

                            if (tokenUtils.validateToken(jwtToken, user)) {
                                Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                                accessor.setUser(auth);
                                System.out.println("User set in accessor and security context: " + auth.getName());
                            }
                        }
                    }

                }
                else {
                    if(accessor.getUser() != null) {
                        System.out.println("Command: " + accessor.getCommand() + ", and user set in accessor and security context: " + accessor.getUser().getName());
                    }
                    else{
                        System.out.println("User is still not saved even if we set him during CONNECT command.");
                    }
                }
                return message;
            }
        });
    }
}
