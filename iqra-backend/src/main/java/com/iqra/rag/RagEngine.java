package com.iqra.rag;

import com.iqra.model.vo.AskResponse;
import com.iqra.model.vo.ReferenceVO;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main RAG engine: orchestrates recall, rerank, prompt building, and LLM generation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEngine {

    private final MultiRecallEngine multiRecallEngine;
    private final RerankerEngine rerankerEngine;
    private final PromptEngine promptEngine;
    private final LlmService llmService;

    public AskResponse execute(String question, String modelId, Long skillId, String history, Object filters) {
        long startTime = System.currentTimeMillis();

        log.info("RAG execution start: question={}, modelId={}", question, modelId);

        // Step 1: Multi-recall
        List<MultiRecallEngine.RecallResult> recallResults = multiRecallEngine.recall(question, filters != null ? (java.util.Map<String, Object>) filters : new java.util.HashMap<>());
        log.info("Recall results: {}", recallResults.size());

        if (recallResults.isEmpty()) {
            return buildNoResultResponse(modelId, startTime);
        }

        // Step 2: Rerank
        List<String> documents = recallResults.stream()
                .map(MultiRecallEngine.RecallResult::getContent)
                .collect(Collectors.toList());

        List<RerankerEngine.RerankResult> rerankResults = rerankerEngine.rerank(question, documents, 5);
        log.info("Rerank results: {}", rerankResults.size());

        // Step 3: Build context from top reranked results
        List<String> topSegments = rerankResults.stream()
                .map(RerankerEngine.RerankResult::getDocument)
                .collect(Collectors.toList());

        String context = promptEngine.buildContext(topSegments);

        // Step 4: Build prompt
        String prompt = promptEngine.buildPrompt(question, context, history, modelId, skillId);

        // Step 5: Get LLM and generate response
        ChatLanguageModel chatModel = llmService.getChatModel(modelId);
        String answer = chatModel.generate(prompt);

        // Step 6: Build response
        long timeCost = System.currentTimeMillis() - startTime;

        AskResponse response = new AskResponse();
        response.setAnswer(answer);
        response.setModelUsed(modelId);
        response.setTime(timeCost);
        response.setReferences(buildReferences(recallResults, rerankResults));

        log.info("RAG execution completed in {}ms", timeCost);
        return response;
    }

    private AskResponse buildNoResultResponse(String modelId, long startTime) {
        AskResponse response = new AskResponse();
        response.setAnswer("抱歉，未能找到相关参考文档。");
        response.setModelUsed(modelId);
        response.setTime(System.currentTimeMillis() - startTime);
        response.setReferences(java.util.Collections.emptyList());
        return response;
    }

    private List<ReferenceVO> buildReferences(List<MultiRecallEngine.RecallResult> recallResults,
                                               List<RerankerEngine.RerankResult> rerankResults) {
        List<ReferenceVO> references = new java.util.ArrayList<>();
        for (RerankerEngine.RerankResult rerank : rerankResults) {
            ReferenceVO vo = new ReferenceVO();
            vo.setContent(rerank.getDocument());
            vo.setScore(rerank.getScore());

            // Match back to recall result for documentId and chunkIndex
            if (rerank.getIndex() != null && rerank.getIndex() < recallResults.size()) {
                MultiRecallEngine.RecallResult recall = recallResults.get(rerank.getIndex());
                vo.setDocumentId(recall.getDocumentId());
                vo.setChunkIndex(recall.getChunkIndex());
            }

            references.add(vo);
        }
        return references;
    }
}
