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
import org.springframework.context.annotation.Primary;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        System.out.println("🚀🚀🚀 V3: MANUAL GOOGLE GENAI BEAN IS EXECUTING! 🚀🚀🚀");

        GoogleGenAiEmbeddingConnectionDetails connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(apiKey)
                .build();

        GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
                .model("gemini-embedding-001")
                .build();

        return new GoogleGenAiTextEmbeddingModel(connectionDetails, options);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        System.out.println(" BUILDING VECTOR STORE WITH CUSTOM MODEL...");
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        File vectorDbFile = new File("/app/data/local_resume_vectors.json");

        if (vectorDbFile.exists()) {
            vectorStore.load(vectorDbFile);
            System.out.println("Loaded existing Google GenAI vector database from disk.");
        }

        return vectorStore;
    }
}