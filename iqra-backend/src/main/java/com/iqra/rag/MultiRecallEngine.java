package com.iqra.rag;

import com.iqra.common.Constants;
import com.iqra.model.vo.ReferenceVO;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Multi-Recall engine: combines vector search, keyword search, and rule-based recall
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiRecallEngine {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    private static final int RECALL_PER_CHANNEL = 10;
    private static final int TOTAL_CANDIDATE = 30;

    public List<RecallResult> recall(String query, Map<String, Object> filters) {
        List<RecallResult> results = new ArrayList<>();

        // Channel 1: Vector recall
        results.addAll(vectorRecall(query, RECALL_PER_CHANNEL));

        // Channel 2: Keyword recall (simulated via metadata filtering)
        results.addAll(keywordRecall(query, RECALL_PER_CHANNEL, filters));

        // Channel 3: Rule-based recall
        results.addAll(ruleRecall(filters, RECALL_PER_CHANNEL));

        // Deduplicate and rank
        return deduplicateAndRank(results, TOTAL_CANDIDATE);
    }

    private List<RecallResult> vectorRecall(String query, int topK) {
        try {
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(query).content();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, topK);
        List<RecallResult> results = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            RecallResult result = new RecallResult();
            result.setContent(match.embedded().text());
            result.setScore(match.score());
            result.setSource("vector");
            result.setDocumentId(match.embedded().metadata().getString("documentId"));
            result.setChunkIndex(match.embedded().metadata().getInteger("chunkIndex"));
            results.add(result);
        }
        return results;
        } catch (Exception e) {
            log.warn("Vector recall failed", e);
            return Collections.emptyList();
        }
    }

    private List<RecallResult> keywordRecall(String query, int topK, Map<String, Object> filters) {
        // Simulated keyword recall - in production use BM25 or full-text search
        List<RecallResult> results = new ArrayList<>();
        String[] keywords = query.split("\\s+");

        try {
            // Simple keyword matching via embedding store
            for (String keyword : keywords) {
                if (keyword.length() < 2) continue;
                dev.langchain4j.data.embedding.Embedding keywordEmbedding = embeddingModel.embed(keyword).content();
                List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(keywordEmbedding, topK / keywords.length);
                for (EmbeddingMatch<TextSegment> match : matches) {
                    String content = match.embedded().text();
                    if (content.toLowerCase().contains(keyword.toLowerCase())) {
                        RecallResult result = new RecallResult();
                        result.setContent(content);
                        result.setScore(match.score() * 0.8); // Lower weight for keyword
                        result.setSource("keyword");
                        result.setDocumentId(match.embedded().metadata().getString("documentId"));
                        result.setChunkIndex(match.embedded().metadata().getInteger("chunkIndex"));
                        results.add(result);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Keyword recall failed", e);
        }

        return results;
    }

    private List<RecallResult> ruleRecall(Map<String, Object> filters, int topK) {
        // Rule-based recall: match by document type, department, tags
        List<RecallResult> results = new ArrayList<>();
        if (filters == null || filters.isEmpty()) {
            return results;
        }

        try {
            // Filter by department or tags if available
            if (filters.containsKey("department")) {
                dev.langchain4j.data.embedding.Embedding deptEmbedding = embeddingModel.embed((String) filters.get("department")).content();
                List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(deptEmbedding, topK);
                for (EmbeddingMatch<TextSegment> match : matches) {
                    RecallResult result = new RecallResult();
                    result.setContent(match.embedded().text());
                    result.setScore(match.score() * 0.6);
                    result.setSource("rule");
                    result.setDocumentId(match.embedded().metadata().getString("documentId"));
                    result.setChunkIndex(match.embedded().metadata().getInteger("chunkIndex"));
                    results.add(result);
                }
            }
        } catch (Exception e) {
            log.warn("Rule recall failed", e);
        }

        return results;
    }

    private List<RecallResult> deduplicateAndRank(List<RecallResult> results, int topK) {
        // Deduplicate by content hash + documentId
        Set<String> seen = new HashSet<>();
        List<RecallResult> unique = new ArrayList<>();

        for (RecallResult result : results) {
            String key = result.getDocumentId() + "_" + result.getChunkIndex();
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(result);
            }
        }

        // Rank by score
        unique.sort(Comparator.comparingDouble(RecallResult::getScore).reversed());

        return unique.stream().limit(topK).collect(Collectors.toList());
    }

    public List<ReferenceVO> toReferences(List<RecallResult> results) {
        List<ReferenceVO> references = new ArrayList<>();
        for (RecallResult result : results) {
            ReferenceVO vo = new ReferenceVO();
            vo.setDocumentId(result.getDocumentId());
            vo.setContent(result.getContent());
            vo.setScore(result.getScore());
            vo.setChunkIndex(result.getChunkIndex());
            references.add(vo);
        }
        return references;
    }

    public static class RecallResult {
        private String content;
        private double score;
        private String source;
        private String documentId;
        private Integer chunkIndex;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public Integer getChunkIndex() { return chunkIndex; }
        public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }
    }
}
