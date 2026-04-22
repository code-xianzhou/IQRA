package com.iqra.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Reranker using bge-reranker-base model
 */
@Slf4j
@Component
public class RerankerEngine {

    @Value("${langchain4j.reranker.base-url:http://localhost:11438}")
    private String baseUrl;

    @Value("${langchain4j.reranker.model-name:bge-reranker-base}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<RerankResult> rerank(String query, List<String> documents, int topK) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<RerankResult> results = callRerankerModel(query, documents);
            results.sort(Comparator.comparingDouble(RerankResult::getScore).reversed());
            return results.stream().limit(topK).toList();
        } catch (Exception e) {
            log.warn("Reranker failed, using original order", e);
            // Fallback: return original order with default score
            List<RerankResult> fallback = new ArrayList<>();
            for (int i = 0; i < Math.min(documents.size(), topK); i++) {
                RerankResult result = new RerankResult();
                result.setIndex(i);
                result.setDocument(documents.get(i));
                result.setScore(0.5);
                fallback.add(result);
            }
            return fallback;
        }
    }

    private List<RerankResult> callRerankerModel(String query, List<String> documents) {
        // Ollama-compatible API call for reranker
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("query", query);
        requestBody.put("documents", documents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = baseUrl + "/api/rerank";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        List<RerankResult> results = new ArrayList<>();

        if (responseBody != null && responseBody.containsKey("results")) {
            List<Map<String, Object>> resultsList = (List<Map<String, Object>>) responseBody.get("results");
            for (Map<String, Object> item : resultsList) {
                RerankResult result = new RerankResult();
                result.setIndex((Integer) item.get("index"));
                result.setScore((Double) item.get("score"));
                int index = (Integer) item.get("index");
                result.setDocument(documents.get(index));
                results.add(result);
            }
        }

        return results;
    }

    public static class RerankResult {
        private Integer index;
        private String document;
        private double score;

        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }
        public String getDocument() { return document; }
        public void setDocument(String document) { this.document = document; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
}
