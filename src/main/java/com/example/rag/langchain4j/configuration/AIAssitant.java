package com.example.rag.langchain4j.configuration;

import com.example.rag.langchain4j.messagespecs.StreamingSupportAgent;
import com.example.rag.langchain4j.messagespecs.SupportAgent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
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
    public StreamingSupportAgent streamingSupportAgent(StreamingChatLanguageModel streamingChatLanguageModel,
                                                       ContentRetriever contentRetriever){
        return AiServices.builder(StreamingSupportAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                      EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .minScore(0.6)
                .maxResults(2)
                .build();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(JdbcConnectionDetails jdbcConnectionDetails){
        String userName = jdbcConnectionDetails.getUsername();
        String password = jdbcConnectionDetails.getPassword();
        return new CustomPgVectorEmbeddingStore("localhost", 5432, userName, password,
                "vector_store", "vector_store");

//        return PgVectorEmbeddingStore.builder()
//                .host("localhost")
//                .port(5432)
//                .user(jdbcConnectionDetails.getUsername())
//                .password(jdbcConnectionDetails.getPassword())
//                .database("vector_store")
//                .table("vector_store_2")
//                .dimension(1536)
//                .build();
    }

}
