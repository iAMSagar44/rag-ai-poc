package com.example.rag.langchain4j.service;

import com.example.rag.langchain4j.messagespecs.StreamingSupportAgent;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Endpoint
@AnonymousAllowed
public class StreamingSupportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingSupportService.class);
    private final StreamingSupportAgent streamingSupportAgent;

    public StreamingSupportService(StreamingSupportAgent streamingSupportAgent) {
        this.streamingSupportAgent = streamingSupportAgent;
    }

    public Flux<String> streamChat(String message) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        streamingSupportAgent.chat(message)
                .onNext(sink::tryEmitNext)
                .onComplete(aiMessageResponse -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }
}
