package com.example.rag;

import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {org.springframework.ai.autoconfigure.vectorstore.azure.AzureVectorStoreAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //Log which implementation of Embedding Client and Chat client is being used
    @Bean
    ApplicationRunner applicationRunner(StreamingChatClient chatClient, EmbeddingClient embeddingClient){
        return args -> {
            System.out.println("Chat Client: " + chatClient.getClass().getName());
            System.out.println("Embedding Client: " + embeddingClient.getClass().getName());
        };
    }

}
