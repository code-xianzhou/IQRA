package com.iqra.controller;

import com.iqra.common.Result;
import com.iqra.model.dto.AskRequest;
import com.iqra.model.vo.AskResponse;
import com.iqra.model.vo.ChatHistoryVO;
import com.iqra.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public Result<AskResponse> ask(@RequestBody AskRequest request) {
        AskResponse response = chatService.ask(request);
        return Result.success(response);
    }

    @GetMapping("/history")
    public Result<List<ChatHistoryVO>> history(@RequestParam String userId,
                                                @RequestParam(required = false) String sessionId) {
        List<ChatHistoryVO> history = chatService.getHistory(userId, sessionId);
        return Result.success(history);
    }

    @DeleteMapping("/history")
    public Result<Void> deleteHistory(@RequestParam String userId,
                                       @RequestParam String sessionId) {
        chatService.deleteHistoryBySession(userId, sessionId);
        return Result.success();
    }
}
