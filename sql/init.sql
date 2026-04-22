-- IQRA Database Initialization Script

CREATE DATABASE IF NOT EXISTS iqra_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE iqra_db;

-- User table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `department` VARCHAR(100) COMMENT '部门',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工用户表';

-- Document metadata table
CREATE TABLE IF NOT EXISTS `document` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `file_type` VARCHAR(20) COMMENT '文件格式: pdf/doc/docx/txt',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `upload_by` VARCHAR(50) COMMENT '上传人',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '解析状态: PENDING/PROCESSING/SUCCESS/FAILED',
    `department` VARCHAR(100) COMMENT '所属部门',
    `tags` VARCHAR(500) COMMENT '标签',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (`status`),
    INDEX idx_upload_by (`upload_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档元数据表';

-- Document chunk table
CREATE TABLE IF NOT EXISTS `document_chunk` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chunk_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '分块唯一ID',
    `document_id` BIGINT NOT NULL COMMENT '关联文档ID',
    `content` TEXT COMMENT '分块文本内容',
    `chunk_index` INT COMMENT '分块序号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_document_id (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分块表';

-- Chat history table
CREATE TABLE IF NOT EXISTS `chat_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `session_id` VARCHAR(64) COMMENT '会话ID',
    `question` TEXT NOT NULL COMMENT '用户问题',
    `answer` TEXT COMMENT '助手回答',
    `references_json` TEXT COMMENT '引用文档JSON',
    `model_used` VARCHAR(50) COMMENT '使用的大模型',
    `skill_used` VARCHAR(50) COMMENT '使用的技能',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (`user_id`),
    INDEX idx_session_id (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';

-- Skill config table
CREATE TABLE IF NOT EXISTS `skill_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `skill_name` VARCHAR(100) NOT NULL COMMENT '技能名称',
    `skill_type` VARCHAR(50) NOT NULL COMMENT '技能类型: DOC_QA/PROCESS/TOOL_QUERY',
    `description` TEXT COMMENT '技能描述',
    `prompt_template` TEXT COMMENT 'Prompt模板',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    `recall_strategy` VARCHAR(50) DEFAULT 'multi' COMMENT '召回策略: multi/keyword/vector',
    `top_k` INT DEFAULT 5 COMMENT '返回数量',
    `rerank_threshold` DOUBLE DEFAULT 0.5 COMMENT '重排阈值',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能配置表';

-- Prompt template table
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(50) NOT NULL COMMENT '模板类型: SYSTEM/SKILL/FUNCTION',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `model_id` VARCHAR(50) COMMENT '关联模型ID',
    `skill_type` VARCHAR(50) COMMENT '关联技能类型',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板表';

-- MCP config table
CREATE TABLE IF NOT EXISTS `mcp_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(255) COMMENT '配置说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型控制配置表';

-- Function info table
CREATE TABLE IF NOT EXISTS `function_info` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `function_name` VARCHAR(100) NOT NULL COMMENT '函数名称',
    `function_desc` TEXT COMMENT '函数描述',
    `parameters` TEXT COMMENT '参数定义JSON',
    `return_type` VARCHAR(50) COMMENT '返回类型',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='函数信息表';

-- Model config table
CREATE TABLE IF NOT EXISTS `model_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `model_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '模型标识',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `model_type` VARCHAR(50) COMMENT '模型类型',
    `base_url` VARCHAR(255) COMMENT '基础URL',
    `port` INT COMMENT '端口',
    `api_path` VARCHAR(255) COMMENT 'API路径',
    `timeout` INT COMMENT '超时时间(ms)',
    `temperature` DOUBLE DEFAULT 0.7 COMMENT '温度参数',
    `max_tokens` INT DEFAULT 4096 COMMENT '最大Token数',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认模型',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/inactive/error',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大模型配置表';

-- Insert default data

-- Default user (password: admin123)
INSERT INTO `user` (`username`, `password`, `real_name`, `department`, `status`) VALUES
('admin', 'admin123', 'Admin', 'TechDept', 1),
('zhangsan', '123456', 'ZhangSan', 'TechDept', 1);

-- Default models
INSERT INTO `model_config` (`model_id`, `model_name`, `model_type`, `base_url`, `port`, `api_path`, `timeout`, `temperature`, `max_tokens`, `enabled`, `is_default`, `status`) VALUES
('qwen2-7b', 'Qwen2-7B', 'open_source', 'http://localhost', 11434, '/v1/chat/completions', 60000, 0.7, 4096, 1, 1, 'active'),
('llama3-8b', 'Llama3-8B', 'open_source', 'http://localhost', 11435, '/v1/chat/completions', 60000, 0.7, 4096, 1, 0, 'active'),
('chatglm3-6b', 'ChatGLM3-6B', 'open_source', 'http://localhost', 11436, '/v1/chat/completions', 60000, 0.7, 4096, 1, 0, 'active');

-- Default skills
INSERT INTO `skill_config` (`skill_name`, `skill_type`, `description`, `prompt_template`, `enabled`, `recall_strategy`, `top_k`, `rerank_threshold`) VALUES
('DocQA', 'DOC_QA', 'Document based QA', 'You are a knowledge assistant. Based on the following reference documents:\n\nReference: {{context}}\nQuestion: {{question}}\n\nPlease give accurate and concise answers.', 1, 'multi', 5, 0.5),
('Process', 'PROCESS', 'Process handling for leave, reimbursement, onboarding', 'You are a process guide assistant. Question: {{question}}\n\nProvide detailed process steps based on reference documents.\n\nReference: {{context}}', 1, 'multi', 5, 0.4),
('ToolQuery', 'TOOL_QUERY', 'Query employee info and attendance', 'You are a data query assistant.\nQuestion: {{question}}\nAnswer based on system provided data.', 1, 'keyword', 3, 0.6);

-- Default prompt template
INSERT INTO `prompt_template` (`template_name`, `template_type`, `content`) VALUES
('DefaultSystemPrompt', 'SYSTEM', 'You are an enterprise internal intelligent assistant. You must answer based on provided reference documents and cannot fabricate information.\n\nReference documents: {{context}}\n\nUser question: {{question}}\n\nHistory: {{history}}\n\nPlease answer in concise, formal, and accurate Chinese.');

-- Default MCP config
INSERT INTO `mcp_config` (`config_key`, `config_value`, `description`) VALUES
('max_qps_per_user', '5', 'Max QPS per user'),
('enable_reranker', 'true', 'Enable reranker'),
('recall_count_vector', '10', 'Vector recall count'),
('recall_count_keyword', '10', 'Keyword recall count'),
('recall_count_rule', '10', 'Rule recall count'),
('rerank_top_k', '5', 'Rerank top K');

-- Default functions
INSERT INTO `function_info` (`function_name`, `function_desc`, `parameters`, `return_type`, `enabled`) VALUES
('getEmployeeInfo', 'Query employee basic info', '{"userId": "string"}', 'string', 1),
('getAttendance', 'Query attendance', '{"userId": "string"}', 'string', 1),
('getLeaveProcess', 'Query leave process', '{}', 'string', 1),
('getReimburseMaterial', 'Query reimbursement material', '{}', 'string', 1);
