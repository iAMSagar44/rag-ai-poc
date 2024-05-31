package com.example.rag.services;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Endpoint
@AnonymousAllowed
public class ChatAgent {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    @Value("classpath:/prompts/prompt-template-2.st")
    private Resource systemPromptResource;

    public ChatAgent(ChatClient.Builder modelBuilder, VectorStore vectorStore) {
        this.chatClient = modelBuilder.build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> generateResponse(String chatId, String userMessage) {
        return this.chatClient.prompt()
                .system(systemPromptResource)
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .user(userMessage)
                .stream()
                .content();
    }
}
