package com.example.rag.azureopenai;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.springframework.ai.azure.openai.AzureOpenAiChatClient;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

@Endpoint
@AnonymousAllowed
@Profile("azureopenai")
public class StreamingCompletionChatService {
    private final AzureOpenAiChatClient azureChatClient;

    public StreamingCompletionChatService(AzureOpenAiChatClient azureChatClient) {
        this.azureChatClient = azureChatClient;
    }

    public Flux<String> generateResponse(String message) {
        return azureChatClient.stream(new Prompt(message))
                .map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                .onBackpressureBuffer()
                .onErrorComplete();
    }
}
