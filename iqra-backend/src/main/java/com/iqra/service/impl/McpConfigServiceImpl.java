package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iqra.mapper.McpConfigMapper;
import com.iqra.model.entity.McpConfig;
import com.iqra.service.McpConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class McpConfigServiceImpl extends ServiceImpl<McpConfigMapper, McpConfig> implements McpConfigService {

    @Override
    public Map<String, String> getAllConfig() {
        List<McpConfig> configs = this.list();
        Map<String, String> result = new HashMap<>();
        for (McpConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    public void updateConfig(String key, String value) {
        LambdaQueryWrapper<McpConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(McpConfig::getConfigKey, key);
        McpConfig config = this.getOne(wrapper);

        if (config == null) {
            config = new McpConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setCreateTime(LocalDateTime.now());
        } else {
            config.setConfigValue(value);
        }
        config.setUpdateTime(LocalDateTime.now());
        this.saveOrUpdate(config);
    }

    @Override
    public void initDefaultMcpConfig() {
        if (this.count() > 0) {
            return;
        }

        updateConfig("max_qps_per_user", "5");
        updateConfig("enable_reranker", "true");
        updateConfig("recall_count_vector", "10");
        updateConfig("recall_count_keyword", "10");
        updateConfig("recall_count_rule", "10");
        updateConfig("rerank_top_k", "5");
    }
}
