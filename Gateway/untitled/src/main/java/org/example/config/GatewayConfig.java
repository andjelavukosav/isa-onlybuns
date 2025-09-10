package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GatewayConfig {

    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier() {
        return new ServiceInstanceListSupplier() {
            @Override
            public String getServiceId() {
                return "backend-service"; // isti kao u lb://backend-service
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                List<ServiceInstance> instances = Arrays.asList(
                        new DefaultServiceInstance("backend1", "backend-service", "localhost", 8080, false),
                        new DefaultServiceInstance("backend2", "backend-service", "localhost", 8081, false),
                        new DefaultServiceInstance("backend3", "backend-service", "localhost", 8082, false)
                );
                return Flux.just(instances);
            }
        };
    }
}
