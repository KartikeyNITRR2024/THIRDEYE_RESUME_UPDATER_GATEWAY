package com.thirdeye30.resumehelper.gateway.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayFilterConfig implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(GatewayFilterConfig.class);

    @Value("${thirdeye.resume.updater.api.key}")
    private String apiKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            var mutatedExchange = exchange.mutate()
                    .request(exchange.getRequest()
                            .mutate()
                            .header("THIRDEYE_RESUME_UPDATER_API_KEY", apiKey)
                            .build())
                    .build();
            
            return chain.filter(mutatedExchange);
        } catch (Exception ex) {
            logger.error("Failed to append API key header to downstream request: {}", ex.getMessage());
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
       return -1;
    }
}