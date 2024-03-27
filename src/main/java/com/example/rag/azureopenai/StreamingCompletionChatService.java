package com.example.rag.azureopenai;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Endpoint
@AnonymousAllowed
public class StreamingCompletionChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingCompletionChatService.class);
    private final StreamingChatClient azureChatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/no-info-prompt-template.st")
    private Resource emptyPromptResource;
    @Value("classpath:/prompts/prompt-template.st")
    private Resource systemPromptResource;

    @Autowired
    public StreamingCompletionChatService(StreamingChatClient azureChatClient, VectorStore vectorStore) {
        this.azureChatClient = azureChatClient;
        this.vectorStore = vectorStore;
    }

//    public Flux<String> generateResponse(String message) {
//        return azureChatClient.stream(new Prompt(message))
//                .map(chatResponse -> chatResponse.getResult().getOutput().getContent())
//                .onBackpressureBuffer()
//                .onErrorComplete();
//    }

    public Flux<String> generateResponse(String message) {
        SystemMessage systemMessage = generateSystemMessage(message);
        //LOGGER.info("The system prompt is -- {}", systemMessage.getContent());
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return azureChatClient.stream(prompt)
                .map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                .onBackpressureBuffer()
                .onErrorComplete();
    }

    private SystemMessage generateSystemMessage(String message) {
        LOGGER.info("Retrieving documents");
        List<Document> similarDocuments = vectorStore
                .similaritySearch(SearchRequest.query(message));
        LOGGER.info("Found {} similar documents", similarDocuments.size());
        if(similarDocuments.isEmpty()) {
            SystemPromptTemplate emptyPromptTemplate = new SystemPromptTemplate(this.emptyPromptResource);
            return (SystemMessage) emptyPromptTemplate.createMessage(Map.of("message",
                    String.format("No information found in the documents for the following question - %s", message)));
        }
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptResource);
        String documentContent = similarDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n"));
        Set<String> collect = similarDocuments.stream().map(Document::getMetadata).map(m -> (String)m.get("file_name")).collect(Collectors.toSet());
        String fileNames = String.join(",", collect);
        //LOGGER.info("The file names are -> {}", fileNames);
        return (SystemMessage) systemPromptTemplate.createMessage(Map.of("documents", documentContent, "fileNames", fileNames));
    }
}
