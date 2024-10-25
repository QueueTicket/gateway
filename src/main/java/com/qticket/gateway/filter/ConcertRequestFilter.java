package com.qticket.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import reactor.core.publisher.Mono;

@Component
public class ConcertRequestFilter extends AbstractGatewayFilterFactory<ConcertRequestFilter.Config> {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "concert-queue";

    @Autowired
    public ConcertRequestFilter(KafkaTemplate<String, String> kafkaTemplate) {
        super(Config.class);
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String originalUrl = exchange.getRequest().getURI().toString();

            if (!originalUrl.startsWith("http://localhost:19091/waiting-room") &&
                    !originalUrl.startsWith("http://localhost:19091/api/v1/queue")) {
                kafkaTemplate.send(TOPIC, originalUrl);
            }

            // 계속해서 다음 필터로 요청을 전달
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // 추가 설정이 필요하면 여기에 정의
    }
}
