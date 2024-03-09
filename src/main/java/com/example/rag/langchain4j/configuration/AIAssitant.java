package com.example.rag.langchain4j.configuration;

import com.example.rag.langchain4j.messagespecs.StreamingSupportAgent;
import com.example.rag.langchain4j.messagespecs.SupportAgent;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIAssitant {

    @Bean
    public SupportAgent supportAgent(ChatLanguageModel chatLanguageModel){
        return AiServices.builder(SupportAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    @Bean
    public StreamingSupportAgent streamingSupportAgent(StreamingChatLanguageModel streamingChatLanguageModel){
        return AiServices.builder(StreamingSupportAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

}
