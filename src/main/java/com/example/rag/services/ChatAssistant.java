package com.example.rag.services;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

@Endpoint
@AnonymousAllowed
public class ChatAssistant {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAssistant.class);
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/prompt-template-2.st")
    private Resource systemPromptResource;

    public ChatAssistant(ChatClient.Builder chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> generateResponse(String chatId, String message) {
        //LOGGER.info("The user message is -- {}", message);
        return chatClient.prompt()
                .system(systemPromptResource)
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .advisors(new LoggingAdvisor())
                .user(message)
                .stream()
                .content();
    }
}
