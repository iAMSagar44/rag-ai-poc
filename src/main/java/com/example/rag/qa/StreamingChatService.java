package com.example.rag.qa;


import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Endpoint
@AnonymousAllowed
@Profile("openai")
public class StreamingChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingChatService.class);
    private final StreamingChatClient chatClient;

    @Value("classpath:/prompts/prompt-template.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/no-info-prompt-template.st")
    private Resource emptyPromptResource;
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
        SystemMessage systemMessage = generateSystemMessage(message);
        //LOGGER.info("The system prompt is -- {}", systemMessage.getContent());
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.stream(prompt)
                .map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                .onBackpressureBuffer()
                .onErrorComplete();
    }

    private SystemMessage generateSystemMessage(String message) {
        LOGGER.info("Retrieving documents");
        List<Document> similarDocuments = vectorStore
                .similaritySearch(SearchRequest.query(message)
                .withTopK(2).withSimilarityThreshold(0.75));
        LOGGER.info("Found {} similar documents", similarDocuments.size());
        if(similarDocuments.isEmpty()) {
            SystemPromptTemplate emptyPromptTemplate = new SystemPromptTemplate(this.emptyPromptResource);
            return (SystemMessage) emptyPromptTemplate.createMessage(Map.of("message",
                    String.format("No information found in the documents for the following question - %s", message)));
        }
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptResource);
        String documentContent = similarDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n"));
        return (SystemMessage) systemPromptTemplate.createMessage(Map.of("documents", documentContent));
    }
}
