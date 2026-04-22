package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.entity.McpConfig;

import java.util.Map;

public interface McpConfigService extends IService<McpConfig> {

    Map<String, String> getAllConfig();

    void updateConfig(String key, String value);

    void initDefaultMcpConfig();
}
