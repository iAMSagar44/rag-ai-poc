package com.example.rag.azureopenai;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

/**
 * Simple, in memory, message chat history built specific for Azure Open AI implementation.
 * This is based of an example provided by Christian Tzolov, that was specific for Open AI.
 * https://github.com/spring-projects/spring-ai/issues/396
 */

@Service
public class InMemChatHistory {

    private static final Logger logger = LoggerFactory.getLogger(InMemChatHistory.class);

    private final Map<String, List<Message>> chatHistoryLog;

    /**
     * Temporal storage used to aggregate streaming messages until the finishReason=STOP is received.
     */
    private final Map<UUID, List<Message>> messageAggregations;

    public InMemChatHistory() {
        this.chatHistoryLog = new ConcurrentHashMap<>();
        this.messageAggregations = new ConcurrentHashMap<>();
    }

    public void addUserMessage(String chatId, Message message) {
        this.commitToHistoryLog(chatId, message);
    }

    public void addAssistantMessage(String chatId, Message message, String finishReason, UUID uuid) {
        this.messageAggregations.putIfAbsent(uuid, new ArrayList<>());
        if (this.messageAggregations.keySet().size() > 1) {
            logger.warn("Multiple active sessions: " + this.messageAggregations.keySet());
        }
        this.messageAggregations.get(uuid).add(message);
        if (finishReason.equalsIgnoreCase("STOP")) {
            this.finalizeMessageGroup(chatId, uuid);
        }
    }

    private void finalizeMessageGroup(String chatId, UUID uuid) {
        if (this.messageAggregations.containsKey(uuid)) {
            List<Message> assistantSessionMessages = this.messageAggregations.get(uuid);
                String aggregatedContent = assistantSessionMessages.stream()
                        .filter(m -> m.getContent() != null)
                        .map(Message::getContent).collect(Collectors.joining());
                logger.info("The assistant message is :: {}", aggregatedContent);
                this.commitToHistoryLog(chatId, new AssistantMessage(aggregatedContent));
            this.messageAggregations.remove(uuid);
        }
        else {
            logger.warn("No active session for groupId: " + uuid);
        }
    }

    private void commitToHistoryLog(String chatId, Message message) {
        this.chatHistoryLog.putIfAbsent(chatId, new ArrayList<>());
        this.chatHistoryLog.get(chatId).add(message);
    }

    public List<Message> getAll(String chatId) {
        if (!this.chatHistoryLog.containsKey(chatId)) {
            return List.of();
        }
        return this.chatHistoryLog.get(chatId);
    }

    public List<Message> getLastN(String chatId, int lastN) {
        if (!this.chatHistoryLog.containsKey(chatId)) {
            logger.debug("No chat history found for {} key", chatId);
            return List.of();
        }
        List<Message> response = this.chatHistoryLog.get(chatId);
        if (this.chatHistoryLog.get(chatId).size() < lastN) {
            return response;
        }

        int from = response.size() - lastN;
        int to = response.size();
        logger.debug("Returning last {} messages from {} to {}", lastN, from, to);

        var responseWindow = response.subList(from, to);
        logger.debug("Returning last {} messages: {}", lastN, responseWindow);

        return responseWindow;
    }
}