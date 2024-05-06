package com.example.rag.qa;


import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Endpoint
@AnonymousAllowed
public class StreamingChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingChatService.class);
    private final StreamingChatClient chatClient;
    private static final int CHAT_HISTORY_WINDOW_SIZE = 40;

    @Value("classpath:/prompts/prompt-template.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/no-info-prompt-template.st")
    private Resource emptyPromptResource;

    private final VectorStore vectorStore;
    private final ChatHistory chatHistory;

    @Autowired
    public StreamingChatService(StreamingChatClient chatClient, VectorStore vectorStore, ChatHistory chatHistory) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.chatHistory = chatHistory;
    }

    public Flux<String> generateResponse(String chatId, String message) {
        SystemMessage systemMessage = (SystemMessage) generateSystemMessage(chatId, message).get("systemMessage");
        String fileNames = (String) generateSystemMessage(chatId, message).get("fileNames");
        //LOGGER.info("The system prompt is -- {}", systemMessage.getContent());
        LOGGER.debug("The file names are -- {}", fileNames);
        UserMessage userMessage = new UserMessage(message);

        chatHistory.addMessage(chatId, userMessage);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        final var responseFlux = chatClient.stream(prompt)
                .map(chatResponse -> {
                    AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
                    chatHistory.addMessage(chatId, assistantMessage);
                    return assistantMessage.getContent();
                })
                .onErrorComplete();

        Mono<String> fileNamesMono = Mono.just("\n \n Files referred to: " + fileNames);
        return responseFlux.concatWith(fileNamesMono);
    }

    private Map<String, Object> generateSystemMessage(String chatId, String message) {
        LOGGER.info("Retrieving documents");
        List<Document> similarDocuments = vectorStore
                .similaritySearch(SearchRequest.query(message));

        LOGGER.info("Found {} similar documents", similarDocuments.size());

        List<Message> messageHistory = chatHistory.getLastN(chatId, CHAT_HISTORY_WINDOW_SIZE);

        String history = messageHistory.stream()
                .map(m -> m.getMessageType().name().toLowerCase() + ": " + m.getContent())
                .collect(Collectors.joining(System.lineSeparator()));

        LOGGER.debug("Conversation History so far:: {}", history);

        if(similarDocuments.isEmpty()) {
            SystemPromptTemplate emptyPromptTemplate = new SystemPromptTemplate(this.emptyPromptResource);
            final var templateMessage = emptyPromptTemplate.createMessage(Map.of("message",
                    String.format("No information found in the documents for the following question - %s", message)));
            return Map.of("systemMessage", templateMessage, "fileNames", Set.of());
        }

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptResource);

        var documentContent = similarDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n"));

        Set<String> fileNames = similarDocuments.stream().map(Document::getMetadata).map(m -> (String)m.get("file_name")).collect(Collectors.toSet());

        String files = String.join(",", fileNames);
        //LOGGER.info("The file names are -> {}", fileNames);
        final var templateMessage = systemPromptTemplate.createMessage(Map.of("documents", documentContent,
                "history", history));
        return Map.of("systemMessage", templateMessage, "fileNames", files);
    }
}
