package com.thirdeye30.resumehelper.gateway.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AiIntegreaterFilter extends AbstractGatewayFilterFactory<AiIntegreaterFilter.Config> {
	
	@Value("${aiintegreater.api.key}")
	private String aiIntegreaterApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiIntegreaterFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("THIRDEYE_AI_INTEGREATER_API_KEY");
            if (apiKey == null || !apiKey.equals(aiIntegreaterApiKey)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                Map<String, Object> errorResponse = new LinkedHashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                errorResponse.put("message", "Invalid Request");
                errorResponse.put("data", null);

                try {
                    byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        private String validApiKey;

        public String getValidApiKey() {
            return validApiKey;
        }

        public void setValidApiKey(String validApiKey) {
            this.validApiKey = validApiKey;
        }
    }
}