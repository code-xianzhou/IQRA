package com.iqra.controller;

import com.iqra.common.Result;
import com.iqra.mcp.McpAspect;
import com.iqra.model.entity.McpConfig;
import com.iqra.service.McpConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpConfigService mcpConfigService;
    private final McpAspect mcpAspect;

    @GetMapping("/config")
    public Result<Map<String, String>> config() {
        Map<String, String> config = mcpConfigService.getAllConfig();
        return Result.success(config);
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody McpConfig config) {
        mcpConfigService.updateConfig(config.getConfigKey(), config.getConfigValue());
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalls", mcpAspect.getTotalCalls());
        stats.put("avgResponseTime", mcpAspect.getAvgResponseTime());
        return Result.success(stats);
    }
}
