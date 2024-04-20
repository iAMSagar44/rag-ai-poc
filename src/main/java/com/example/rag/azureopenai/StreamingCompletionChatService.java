package com.example.rag.azureopenai;

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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;

@Endpoint
@AnonymousAllowed
public class StreamingCompletionChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingCompletionChatService.class);
    private final StreamingChatClient chatClient;
    private static final int CHAT_HISTORY_WINDOW_SIZE = 40;

    @Value("classpath:/prompts/prompt-template.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/no-info-prompt-template.st")
    private Resource emptyPromptResource;

    private final VectorStore vectorStore;
    private final InMemChatHistory chatHistory;

    @Autowired
    public StreamingCompletionChatService(StreamingChatClient chatClient, VectorStore vectorStore, InMemChatHistory chatHistory) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.chatHistory = chatHistory;
    }

    public Flux<String> generateResponse(String chatId, String message) {
        SystemMessage systemMessage = generateSystemMessage(chatId, message);
        //LOGGER.info("The system prompt is -- {}", systemMessage.getContent());
        UserMessage userMessage = new UserMessage(message);
        chatHistory.addUserMessage(chatId, userMessage);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        final var uuid = randomUUID();
        return chatClient.stream(prompt)
                .map(chatResponse -> {
                    String finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    LOGGER.debug("Tracking finish reason --> {}", finishReason);
                    AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
                    chatHistory.addAssistantMessage(chatId, assistantMessage, finishReason, uuid);
                    return assistantMessage.getContent();
                })
                .onBackpressureBuffer()
                .onErrorComplete();
    }

    private SystemMessage generateSystemMessage(String chatId, String message) {
        LOGGER.info("Retrieving documents");
        List<Document> similarDocuments = vectorStore
                .similaritySearch(SearchRequest.query(message));

        LOGGER.info("Found {} similar documents", similarDocuments.size());

        List<Message> messageHistory = chatHistory.getLastN(chatId, CHAT_HISTORY_WINDOW_SIZE);

        var history = messageHistory.stream()
                .map(m -> m.getMessageType().name().toLowerCase() + " : " + m.getContent())
                .collect(Collectors.joining(System.lineSeparator()));

        LOGGER.info("Conversation History so far:: {}", history);

        if(similarDocuments.isEmpty()) {
            SystemPromptTemplate emptyPromptTemplate = new SystemPromptTemplate(this.emptyPromptResource);
            return (SystemMessage) emptyPromptTemplate.createMessage(Map.of("message",
                    String.format("No information found in the documents for the following question - %s", message)));
        }

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptResource);

        var documentContent = similarDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n"));

        Set<String> collect = similarDocuments.stream().map(Document::getMetadata).map(m -> (String)m.get("file_name")).collect(Collectors.toSet());

        String fileNames = String.join(",", collect);
        //LOGGER.info("The file names are -> {}", fileNames);
        return (SystemMessage) systemPromptTemplate.createMessage(Map.of("documents", documentContent,
                "history", history,
                "fileNames", fileNames));
    }
}