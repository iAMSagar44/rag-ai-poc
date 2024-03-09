package com.example.rag.langchain4j.messagespecs;

import dev.langchain4j.service.TokenStream;

public interface StreamingSupportAgent {
    TokenStream chat(String message);

}
