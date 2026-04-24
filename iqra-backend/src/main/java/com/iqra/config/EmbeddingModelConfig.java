package com.iqra.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EmbeddingModelConfig {

    @Value("${langchain4j.embedding.base-url:http://localhost:11437}")
    private String baseUrl;

    @Value("${langchain4j.embedding.model-name:nomic-embed-text}")
    private String modelName;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Initializing embedding model: {} at {}", modelName, baseUrl);
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }
}
