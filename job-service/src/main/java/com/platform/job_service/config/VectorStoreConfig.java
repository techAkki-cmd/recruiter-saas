package com.platform.job_service.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // Spring Boot will automatically inject the Local Transformers Embedder here!
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        File vectorDbFile = new File("local_resume_vectors.json");
        if (vectorDbFile.exists()) {
            vectorStore.load(vectorDbFile);
            System.out.println("📦 Loaded existing vector database from disk.");
        }

        return vectorStore;
    }
}