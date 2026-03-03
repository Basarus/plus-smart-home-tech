package ru.yandex.practicum.telemetry.analyzer.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ManagedChannel hubRouterChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 59090)
                .usePlaintext()
                .build();
    }
}