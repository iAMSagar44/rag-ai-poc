package com.example.rag.qa;


import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Endpoint
@AnonymousAllowed
public class StreamingChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingChatService.class);
    private final StreamingChatClient chatClient;

    @Value("classpath:/prompts/prompt-template.st")
    private Resource systemPromptResource;
    private final VectorStore vectorStore;

    public StreamingChatService(StreamingChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

      public Flux<String> streamChat(String message) {
        Flux<ChatResponse> chatResponseFlux = chatClient.stream(new Prompt(message));
        Flux<String> stringFlux = chatResponseFlux.map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                .onBackpressureBuffer()
                .onErrorComplete();
        //stringFlux.subscribe(s -> LOGGER.info(":::::Streaming data::::: \n {}", s));
        return stringFlux;
    }

    public Flux<String> generateResponse(String message) {
        Message systemMessage = generateSystemMessage(message);
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.stream(prompt)
                .map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                .onBackpressureBuffer()
                .onErrorComplete();
    }

    private Message generateSystemMessage(String message) {
        LOGGER.info("Retrieving documents");
        List<Document> similarDocuments = vectorStore.similaritySearch(message);
        LOGGER.info("Found {} similar documents", similarDocuments.size());
        String documentContent = similarDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n"));
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptResource);
        return systemPromptTemplate.createMessage(Map.of("documents", documentContent));
    }
}
