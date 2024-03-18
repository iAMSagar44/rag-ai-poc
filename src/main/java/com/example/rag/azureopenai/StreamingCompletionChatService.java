package com.example.rag.azureopenai;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

@Endpoint
@AnonymousAllowed
@Profile("azureopenai")
public class StreamingCompletionChatService {

    public Flux<String> generateResponse(String message) {
        return Flux.just("Hi", "there.");
    }
}
