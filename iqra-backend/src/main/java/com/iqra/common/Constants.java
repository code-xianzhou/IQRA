package com.iqra.common;

public class Constants {

    // 文档解析状态
    public static final String DOC_STATUS_PENDING = "PENDING";
    public static final String DOC_STATUS_PROCESSING = "PROCESSING";
    public static final String DOC_STATUS_SUCCESS = "SUCCESS";
    public static final String DOC_STATUS_FAILED = "FAILED";

    // 技能类型
    public static final String SKILL_TYPE_DOC_QA = "DOC_QA";
    public static final String SKILL_TYPE_PROCESS = "PROCESS";
    public static final String SKILL_TYPE_TOOL_QUERY = "TOOL_QUERY";

    // 模型标识
    public static final String MODEL_QWEN2 = "qwen2-7b";
    public static final String MODEL_LLAMA3 = "llama3-8b";
    public static final String MODEL_CHATGLM3 = "chatglm3-6b";

    // 向量维度
    public static final Integer EMBEDDING_DIM = 768;

    // Milvus集合名称
    public static final String MILVUS_COLLECTION_NAME = "internal_kb_collection";

    // 缓存名称
    public static final String CACHE_MODEL_CONFIG = "modelConfig";
    public static final String CACHE_SKILL_CONFIG = "skillConfig";
    public static final String CACHE_PROMPT_TEMPLATE = "promptTemplate";
}
