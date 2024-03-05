package com.example.rag.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;


@Service
public class DataLoadingService {
    private static final Logger logger = LoggerFactory.getLogger(DataLoadingService.class);

    @Value("file://${Home}/Downloads/new-plat-qantas-terms.pdf")
    private Resource pdfResource;

    private final VectorStore vectorStore;

    @Autowired
    public DataLoadingService(VectorStore vectorStore) {
        Assert.notNull(vectorStore, "VectorStore must not be null.");
        this.vectorStore = vectorStore;
    }

    public void load() {
        checkFileExists();
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                this.pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(3)
                                .withNumberOfTopPagesToSkipBeforeDelete(1)
                                // .withLeftAlignment(true)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        var tokenTextSplitter = new TokenTextSplitter();

        logger.info("Parsing document, splitting, creating embeddings and storing in vector store...  this will take a while.");
        this.vectorStore.accept(
                tokenTextSplitter.apply(
                        pdfReader.get()));
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
