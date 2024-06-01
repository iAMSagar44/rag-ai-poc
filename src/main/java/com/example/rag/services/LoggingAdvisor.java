package com.example.rag.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisedRequest;
import org.springframework.ai.chat.client.RequestResponseAdvisor;
import java.util.Map;

public class LoggingAdvisor implements RequestResponseAdvisor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAdvisor.class);
    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        LOGGER.info("Advisor Request: {}", request);
        return request;
    }
}
