USE iqra_db;

UPDATE skill_config SET
    description = '基于文档的智能问答，通过检索企业内部知识库提供准确答案',
    prompt_template = '你是一个知识助手。基于以下参考文档：\n\n参考内容：{{context}}\n问题：{{question}}\n\n请给出准确简洁的回答。'
WHERE skill_name = 'DocQA';

UPDATE skill_config SET
    description = '流程办理助手，支持请假、报销、入职等流程咨询',
    prompt_template = '你是流程引导助手。问题：{{question}}\n\n请基于参考文档提供详细的流程步骤。\n\n参考内容：{{context}}'
WHERE skill_name = 'Process';

UPDATE skill_config SET
    description = '工具调用查询，支持员工信息、考勤等数据查询',
    prompt_template = '你是数据查询助手。\n问题：{{question}}\n请基于系统提供的数据回答。',
    recall_strategy = 'vector'
WHERE skill_name = 'ToolQuery';
