package com.example.rag.langchain4j.configuration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pgvector.PGvector;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

import static dev.langchain4j.internal.Utils.isNotNullOrBlank;


public class CustomPgVectorEmbeddingStore implements EmbeddingStore<TextSegment> {
    private static final Gson GSON = new Gson();
    private final String host;
    private final Integer port;
    private final String user;
    private final String password;
    private final String database;
    private final String table;

    public CustomPgVectorEmbeddingStore(String host, Integer port, String user, String password, String database, String table) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
        this.table = table;
    }

    @Override
    public String add(Embedding embedding) {
        return null;
    }

    @Override
    public void add(String id, Embedding embedding) {
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        return null;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        return null;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> embedded) {
        return null;
    }

    @Override
    public List<EmbeddingMatch<TextSegment>> findRelevant(Embedding referenceEmbedding, int maxResults, double minScore) {
        List<EmbeddingMatch<TextSegment>> result = new ArrayList<>();
        try (Connection connection = setupConnection()) {
            String referenceVector = Arrays.toString(referenceEmbedding.vector());
            String query = String.format(
                    "WITH temp AS (SELECT (2 - (embedding <=> '%s')) / 2 AS score, id, embedding, content, metadata FROM %s) SELECT * FROM temp WHERE score >= %s ORDER BY score desc LIMIT %s;",
                    referenceVector, table, minScore, maxResults);
            PreparedStatement selectStmt = connection.prepareStatement(query);

            ResultSet resultSet = selectStmt.executeQuery();
            while (resultSet.next()) {
                double score = resultSet.getDouble("score");
                String embeddingId = resultSet.getString("id");

                PGvector vector = (PGvector) resultSet.getObject("embedding");
                Embedding embedding = new Embedding(vector.toArray());

                String text = resultSet.getString("content");
                TextSegment textSegment = null;
                if (isNotNullOrBlank(text)) {
                    String metadataJson = Optional.ofNullable(resultSet.getString("metadata")).orElse("{}");
                    Type type = new TypeToken<Map<String, String>>() {
                    }.getType();
                    Metadata metadata = new Metadata(new HashMap<>(GSON.fromJson(metadataJson, type)));
                    textSegment = TextSegment.from(text, metadata);
                }

                result.add(new EmbeddingMatch<>(score, embeddingId, embedding, textSegment));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private Connection setupConnection() throws SQLException{
        Connection connection = DriverManager.getConnection(
                String.format("jdbc:postgresql://%s:%s/%s", host, port, database),
                user,
                password
        );
        //connection.createStatement().executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        PGvector.addVectorType(connection);
        return connection;
    }
}
