package com.iqra.config;

import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private Integer port;

    @Value("${milvus.collectionName:internal_kb_collection}")
    private String collectionName;

    @Value("${milvus.dimension:768}")
    private Integer dimension;

    @Bean
    public MilvusEmbeddingStore milvusEmbeddingStore() {
        log.info("Initializing Milvus connection: {}:{}", host, port);
        return MilvusEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName(collectionName)
                .dimension(dimension)
                .build();
    }
}
