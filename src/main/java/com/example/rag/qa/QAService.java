package com.example.rag.qa;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Endpoint
@AnonymousAllowed
@Service
public class QAService {

    private static final Logger logger = LoggerFactory.getLogger(QAService.class);

    @Value("classpath:/prompts/prompt-template.st")
    private Resource qaSystemPromptResource;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Autowired
    public QAService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    public String generate(String message) {
        Message systemMessage = getSystemMessage(message);
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        logger.info("Asking AI model to reply to question.");
        ChatResponse chatResponse = chatClient.call(prompt);
        logger.info("AI responded.");
        return chatResponse.getResult().getOutput().getContent();
    }

    private Message getSystemMessage(String query) {
            logger.info("Retrieving relevant documents");
            List<Document> similarDocuments = vectorStore.similaritySearch(query);
            logger.info(String.format("Found %s relevant documents.", similarDocuments.size()));
            String documents = similarDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n"));
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.qaSystemPromptResource);
            return systemPromptTemplate.createMessage(Map.of("documents", documents));
    }

    public String openChat(String message) {
        return chatClient.call(message);
    }
}
