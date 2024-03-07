package com.example.rag.qa;


import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;


@Endpoint
@AnonymousAllowed
public class StreamingChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingChatService.class);
    private final StreamingChatClient chatClient;

    public StreamingChatService(StreamingChatClient chatClient) {
        this.chatClient = chatClient;
    }

    //Todo - update to use vector store (RAG pattern)
    public Flux<String> streamChat(String message) {
        Flux<ChatResponse> chatResponseFlux = chatClient.stream(new Prompt(message));
        Flux<String> stringFlux = chatResponseFlux.map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                .onBackpressureBuffer()
                .onErrorComplete();
        //stringFlux.subscribe(s -> LOGGER.info(":::::Streaming data::::: \n {}", s));
        return stringFlux;
    }
}
