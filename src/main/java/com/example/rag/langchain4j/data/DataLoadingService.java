package com.example.rag.langchain4j.data;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;


@Service
public class DataLoadingService {
    private static final Logger logger = LoggerFactory.getLogger(DataLoadingService.class);

    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("file://${Home}/Downloads/employee_handbook.pdf")
    private Resource pdfResource;

    private Tokenizer tokenizer;

    private final EmbeddingModel embeddingModel;

    @Autowired
    public DataLoadingService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }


    public void load() throws IOException {
        checkFileExists();
        var document = loadDocument(pdfResource.getFile().toPath(), new ApachePdfBoxDocumentParser());
        DocumentSplitter documentByParagraphSplitter = new DocumentByParagraphSplitter(100, 0, tokenizer);
        var ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentByParagraphSplitter)
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();
        ingestor.ingest(document);

        logger.info("Done parsing document, splitting, creating embeddings and storing in vector store");
    }

    private void checkFileExists() {
        logger.info("The file exists? {}", this.pdfResource.exists());
        try {
            logger.info("The file name is {}", this.pdfResource.getFile().getName());
        } catch (IOException e) {
            logger.info("The file was not found {}", e.getLocalizedMessage());
        }
    }
}
