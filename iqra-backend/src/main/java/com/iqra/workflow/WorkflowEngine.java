package com.iqra.workflow;

import com.iqra.model.vo.AskResponse;
import com.iqra.rag.LlmService;
import com.iqra.rag.MultiRecallEngine;
import com.iqra.rag.PromptEngine;
import com.iqra.rag.RagEngine;
import com.iqra.rag.RerankerEngine;
import com.iqra.skill.SkillRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Workflow orchestration engine
 * Nodes: input parsing -> intent recognition -> skill routing -> multi-recall -> rerank -> function call check -> prompt -> LLM -> format
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEngine {

    private final IntentRecognizer intentRecognizer;
    private final SkillRegistry skillRegistry;
    private final MultiRecallEngine multiRecallEngine;
    private final RerankerEngine rerankerEngine;
    private final PromptEngine promptEngine;
    private final LlmService llmService;
    private final FunctionCallRouter functionCallRouter;

    public AskResponse execute(WorkflowContext context) {
        long startTime = System.currentTimeMillis();
        log.info("Workflow execution start: userId={}, question={}", context.getUserId(), context.getQuestion());

        try {
            // Node 1: Intent recognition
            String intent = intentRecognizer.recognize(context.getQuestion());
            context.setIntent(intent);
            log.info("Intent recognized: {}", intent);

            // Node 2: Skill routing
            if (context.getSkillId() == null) {
                Long skillId = skillRegistry.routeByIntent(intent);
                context.setSkillId(skillId);
            }
            log.info("Skill routed: {}", context.getSkillId());

            // Node 3: Check if function call is needed
            if (functionCallRouter.needFunctionCall(context)) {
                return handleFunctionCall(context, startTime);
            }

            // Node 4: Multi-recall
            Map<String, Object> filters = buildFilters(context);
            List<MultiRecallEngine.RecallResult> recallResults = multiRecallEngine.recall(context.getQuestion(), filters);
            log.info("Recall results: {}", recallResults.size());

            if (recallResults.isEmpty()) {
                return buildNoReferenceResponse(context.getModelId(), startTime);
            }

            // Node 5: Rerank
            List<String> documents = recallResults.stream()
                    .map(MultiRecallEngine.RecallResult::getContent)
                    .collect(Collectors.toList());

            List<RerankerEngine.RerankResult> rerankResults = rerankerEngine.rerank(context.getQuestion(), documents, 5);
            log.info("Rerank results: {}", rerankResults.size());

            // Node 6: Build context and prompt
            List<String> topSegments = rerankResults.stream()
                    .map(RerankerEngine.RerankResult::getDocument)
                    .collect(Collectors.toList());

            String contextStr = promptEngine.buildContext(topSegments);
            String prompt = promptEngine.buildPrompt(context.getQuestion(), contextStr, context.getHistory(), context.getModelId(), context.getSkillId());

            // Node 7: LLM generation
            String answer = llmService.getChatModel(context.getModelId()).generate(prompt);

            // Node 8: Format result
            long timeCost = System.currentTimeMillis() - startTime;
            return buildResponse(answer, recallResults, rerankResults, context.getModelId(), timeCost);

        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            AskResponse response = new AskResponse();
            response.setAnswer("系统处理异常: " + e.getMessage());
            response.setModelUsed(context.getModelId());
            response.setTime(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    private AskResponse handleFunctionCall(WorkflowContext context, long startTime) {
        log.info("Handling function call for intent: {}", context.getIntent());
        String result = functionCallRouter.execute(context);
        long timeCost = System.currentTimeMillis() - startTime;

        AskResponse response = new AskResponse();
        response.setAnswer(result);
        response.setModelUsed(context.getModelId());
        response.setTime(timeCost);
        response.setReferences(java.util.Collections.emptyList());
        return response;
    }

    private Map<String, Object> buildFilters(WorkflowContext context) {
        Map<String, Object> filters = new java.util.HashMap<>();
        // Add department, tags, etc. based on context
        return filters;
    }

    private AskResponse buildNoReferenceResponse(String modelId, long startTime) {
        AskResponse response = new AskResponse();
        response.setAnswer("抱歉，未能找到相关参考文档。请尝试换一种方式提问。");
        response.setModelUsed(modelId);
        response.setTime(System.currentTimeMillis() - startTime);
        response.setReferences(java.util.Collections.emptyList());
        return response;
    }

    private AskResponse buildResponse(String answer,
                                       List<MultiRecallEngine.RecallResult> recallResults,
                                       List<RerankerEngine.RerankResult> rerankResults,
                                       String modelId, long timeCost) {
        AskResponse response = new AskResponse();
        response.setAnswer(answer);
        response.setModelUsed(modelId);
        response.setTime(timeCost);

        List<com.iqra.model.vo.ReferenceVO> references = new java.util.ArrayList<>();
        for (RerankerEngine.RerankResult rerank : rerankResults) {
            com.iqra.model.vo.ReferenceVO vo = new com.iqra.model.vo.ReferenceVO();
            vo.setContent(rerank.getDocument());
            vo.setScore(rerank.getScore());
            if (rerank.getIndex() != null && rerank.getIndex() < recallResults.size()) {
                MultiRecallEngine.RecallResult recall = recallResults.get(rerank.getIndex());
                vo.setDocumentId(recall.getDocumentId());
                vo.setChunkIndex(recall.getChunkIndex());
            }
            references.add(vo);
        }
        response.setReferences(references);
        return response;
    }
}
