package com.example.rag;


import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //Log which implementation of Vector Store, Embedding Client and Chat client is being used
    @Bean
    ApplicationRunner applicationRunner(StreamingChatModel chatModel, EmbeddingModel embeddingModel, VectorStore vectorStore){
        return args -> {
            System.out.println("Chat Client: " + chatModel.getClass().getName());
            System.out.println("Embedding Client: " + embeddingModel.getClass().getName());
            System.out.println("Vector Store:" + vectorStore.getClass().getName());
        };
    }

}
