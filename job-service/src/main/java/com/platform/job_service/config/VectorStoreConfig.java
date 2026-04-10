package com.platform.job_service.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Bean
    public EmbeddingModel embeddingModel() {
        // Create the connection details using your injected API key
        GoogleGenAiEmbeddingConnectionDetails connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(apiKey)
                .build();

        // Initialize the specific Text Embedding model with default options
        return new GoogleGenAiTextEmbeddingModel(connectionDetails, GoogleGenAiTextEmbeddingOptions.builder().build());
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        File vectorDbFile = new File("local_resume_vectors.json");
        if (vectorDbFile.exists()) {
            vectorStore.load(vectorDbFile);
            System.out.println("☁️ Loaded existing Google GenAI vector database from disk.");
        }

        return vectorStore;
    }
}