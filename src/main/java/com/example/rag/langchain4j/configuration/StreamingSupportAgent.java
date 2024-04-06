package com.example.rag.langchain4j.configuration;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface StreamingSupportAgent {
    @SystemMessage("""
           You are a helpful assistant, conversing with a user about the subjects contained in a set of documents.
           Use the information from the documents to provide accurate answers. If unsure or if the answer
           isn't found in the documents, simply state that you don't know the answer.
           Today is {{current_date}}.
           """)
    TokenStream chat(@UserMessage String message);

}
