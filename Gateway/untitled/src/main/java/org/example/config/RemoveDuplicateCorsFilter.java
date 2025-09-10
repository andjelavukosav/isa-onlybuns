package org.example.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class RemoveDuplicateCorsFilter {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter removeDuplicateCorsHeaderFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().beforeCommit(() -> {
                // Uklanjamo duple Access-Control-Allow-Origin header-e
                exchange.getResponse().getHeaders().set("Access-Control-Allow-Origin", "http://localhost:4200");
                return Mono.empty();
            });
            return chain.filter(exchange);
        };
    }
}
