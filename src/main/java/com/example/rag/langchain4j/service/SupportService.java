package com.example.rag.langchain4j.service;

import com.example.rag.langchain4j.messagespecs.SupportAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class SupportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupportService.class);
    private final SupportAgent supportAgent;

    public SupportService(SupportAgent supportAgent) {
        this.supportAgent = supportAgent;
    }

    public String chat(String message) {
        LOGGER.info("Entering Langchain4J");
        return supportAgent.chat(message);
    }
}
