package com.example.rag.configuration;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ AzureAISearchProperties.class })
public class AzureVectorStoreConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureVectorStoreConfig.class);
    @Bean
    public SearchClient searchClient(AzureAISearchProperties properties) {

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
                .endpoint(properties.getUrl())
                .credential(new AzureKeyCredential(properties.getApiKey()))
                .buildClient();

        boolean indexExists = searchIndexClient.listIndexes().stream()
                .map(SearchIndex::getName)
                .anyMatch(name -> name.equals(properties.getIndexName()));

        if (!indexExists) {
            LOGGER.error("Index {} does not exist. Stopping Application.", properties.getIndexName());
            throw new IllegalArgumentException("Index " + properties.getIndexName() + " does not exist");
        }

        return new SearchClientBuilder()
            .endpoint(properties.getUrl())
            .credential(new AzureKeyCredential(properties.getApiKey()))
            .indexName(properties.getIndexName())
            .buildClient();
    }

    @Bean
    public AzureAISearchVectorStore azVectorStore(SearchClient searchClient, EmbeddingClient embeddingClient, AzureAISearchProperties properties) {
        final var azureAISearchVectorStore = new AzureAISearchVectorStore(searchClient, embeddingClient);
        if (properties.getDefaultTopK() >= 0) {
            azureAISearchVectorStore.setTopK(properties.getDefaultTopK());
        }
        if (properties.getDefaultSimilarityThreshold() >= 0.0) {
            azureAISearchVectorStore.setSimilarityThreshold(properties.getDefaultSimilarityThreshold());
        }
        return azureAISearchVectorStore;
    }
}