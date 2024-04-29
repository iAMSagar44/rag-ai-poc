package com.example.rag.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "azure.aisearch")
public class AzureAISearchProperties {
    @NotNull
    private String url;
    @NotNull
    private String apiKey;
    @NotNull
    private String indexName;

    private int defaultTopK = -1;

    private double defaultSimilarityThreshold = -1;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    public int getDefaultTopK() {
        return defaultTopK;
    }

    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }

    public double getDefaultSimilarityThreshold() {
        return defaultSimilarityThreshold;
    }

    public void setDefaultSimilarityThreshold(double defaultSimilarityThreshold) {
        this.defaultSimilarityThreshold = defaultSimilarityThreshold;
    }
}