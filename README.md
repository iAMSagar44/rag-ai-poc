# Retrieval Augmented Generation based Application

## Work in Progress

This app is an AI-powered customer support chat application that uses an LLM to interact with the user

Technology Stack:

- [Vaadin Hilla](https://vaadin.com) for building the Front End.
- [Spring Boot](https://spring.io/projects/spring-boot#overview) for building the back end.
- [Spring AI](https://spring.io/projects/spring-ai/) for orchestrating calls with the AI models and Vector Store
- [Azure OpenAI](https://learn.microsoft.com/en-us/azure/ai-services/openai/overview) for the AI model
- [Azure AI Search](https://learn.microsoft.com/en-us/azure/search/search-what-is-azure-search) for the Vector Store

## Requirements
- Java 17+
- Azure OpenAI endpoint and API key
- Azure AI Search endpoint and key

## Running the application
Run the app by running `Application.java` in your IDE or `./mvnw spring-boot:run` in the command line.

## Running
Also exploring other AI models like Open AI and vector databases like PgVector. Also giving Langchain4j a try.
Please check other branches for their implementations.