package com.platform.job_service.config;

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
    public GoogleGenAiTextEmbeddingModel googleEmbeddingModel() {
        GoogleGenAiEmbeddingConnectionDetails connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(apiKey)
                .build();

        return new GoogleGenAiTextEmbeddingModel(connectionDetails, GoogleGenAiTextEmbeddingOptions.builder().build());
    }

    @Bean
    public VectorStore vectorStore(GoogleGenAiTextEmbeddingModel googleEmbeddingModel) {
        // Now it's looking for the EXACT class we just defined above
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(googleEmbeddingModel).build();

        File vectorDbFile = new File("local_resume_vectors.json");
        if (vectorDbFile.exists()) {
            vectorStore.load(vectorDbFile);
            System.out.println("☁️ Loaded existing Google GenAI vector database from disk.");
        }

        return vectorStore;
    }
}