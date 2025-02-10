package pers.fulsun.cleanup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pers.fulsun.cleanup.model.dto.CleanupRequest;
import pers.fulsun.cleanup.service.SseService;

import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
public class SseController {


    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    // 前端连接SSE的端点
    @GetMapping("/connect/{taskId}")
    public SseEmitter connect(@PathVariable String taskId) {
        return sseService.createEmitter(taskId);
    }

    // 启动任务
    @PostMapping("/start-tasks")
    public ResponseEntity<String> startCheck(@RequestBody CleanupRequest request) {
        String taskId = UUID.randomUUID().toString();
        sseService.createEmitter(taskId); // 预先创建Emitter
        sseService.startImageCleanTask(taskId,request); // 启动异步任务
        return ResponseEntity.ok(taskId);
    }
}