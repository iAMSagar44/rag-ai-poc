package com.example.rag.configuration;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AzureAISearchVectorStore {
    private final EmbeddingClient embeddingClient;
    private final SearchClient searchClient;

    private static final int DEFAULT_TOP_K = 4;
    private static final Double DEFAULT_SIMILARITY_THRESHOLD = 0.0;
    private int topK = DEFAULT_TOP_K;
    private Double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;

    public AzureAISearchVectorStore(SearchClient searchClient, EmbeddingClient embeddingClient) {
        this.searchClient = searchClient;
        this.embeddingClient = embeddingClient;
    }

    public List<Document> similaritySearch(SearchRequest request) {

        Assert.notNull(request, "The search request must not be null.");

        var searchEmbedding = toFloatList(embeddingClient.embed(request.getQuery()));

        final var vectorQuery = new VectorizedQuery(searchEmbedding).setKNearestNeighborsCount(getTopK())
                // Set the fields to compare the vector against. This is a comma-delimited
                // list of field names.
                .setFields("embedding");

        var searchOptions = new SearchOptions()
                .setVectorSearchOptions(new VectorSearchOptions().setQueries(vectorQuery));


        final var searchResults = searchClient.search(null, searchOptions, Context.NONE);

        return searchResults.stream()
                .filter(result -> result.getScore() >= getSimilarityThreshold())
                .map(result -> {

                    final AzureSearchDocument entry = result.getDocument(AzureSearchDocument.class);

                    Map<String, Object> metadata = (StringUtils.hasText(entry.metadata()))
                            ? JSONObject.parseObject(entry.metadata(), new TypeReference<Map<String, Object>>() {
                    }) : Map.of();

                    metadata.put("distance", 1 - (float) result.getScore());

                    final Document doc = new Document(entry.id(), entry.content(), metadata);
                    doc.setEmbedding(entry.embedding());

                    return doc;

                })
                .collect(Collectors.toList());
    }

    private List<Float> toFloatList(List<Double> doubleList) {
        return doubleList.stream().map(Double::floatValue).toList();
    }

    /**
     * Internal data structure for retrieving and storing documents.
     */
    private record AzureSearchDocument(String id, String content, List<Double> embedding, String metadata) {
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public Double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(Double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
}
