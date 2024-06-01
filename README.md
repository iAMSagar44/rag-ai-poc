# Retrieval Augmented Generation with Open AI, SpringAI and PgVectorStore

## Introduction

This is a RAG based application using Open AI, Spring AI and PgVectorStore.  The application loads a set of documents into a vector store and then uses the Open AI API to answer questions based on the documents.  The application uses the RAG pattern to stuff the prompt with similar documents to the question.

Technology Stack:

- [Vaadin Hilla](https://vaadin.com) for building the Front End.
- [Spring Boot](https://spring.io/projects/spring-boot#overview) for building the back end.
- [Spring AI](https://spring.io/projects/spring-ai/) for orchestrating calls with the AI models and Vector Store
- [OpenAI](https://platform.openai.com/docs/api-reference) for the AI models.
- [PgVectorStore](https://github.com/pgvector/pgvector) for storing the document vectors.

## Requirements
- Java 17+
- Docker
- OpenAI API Key and endpoint
- PostgreSQL

## Prerequisites

### OpenAI Credentials

Create an account at [OpenAI Signup](https://platform.openai.com/signup) and generate the token at [API Keys](https://platform.openai.com/account/api-keys).

The Spring AI project defines a configuration property named `spring.ai.openai.api-key` that you should set to the value of the `API Key` obtained from `openai.com`.

You can set this in the projects `/resources/application.yml` file or by exporting an environment variable, for example.
```shell
export SPRING_AI_OPENAI_API_KEY=<INSERT KEY HERE>
```

Note, the `/resources/application.yml` references the environment variable `${SPRING_AI_OPENAI_API_KEY}`.


## Vector Store

This project uses PGvector store to store the document vectors. PGvector is an open-source extension for PostgreSQL that enables storing and searching over machine learning-generated embeddings.

### Running the VectorStore

The VectorStore is a docker container that can be started with the following command:

```
docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres ankane/pgvector
```

Then you can connect to the database (password: `postgres`) and inspect or alter the `vector_store` table content:

```
psql -U postgres -h localhost -p 5432

\l
\c vector_store
\dt

select count(*) from vector_store;

delete from vector_store;
```

## Loading documents into the Vector Store

You can refer to my other project [data-indexer-cli](https://github.com/iAMSagar44/data-indexer-cli/tree/data-loader-local-pgvector) for instructions to load documents into the vector store. This is a CLI based application which can be used to load documents into the vector store.

## Building and running this application

```
./mvnw spring-boot:run
```




