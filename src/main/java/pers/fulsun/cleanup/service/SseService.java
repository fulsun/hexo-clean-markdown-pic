package pers.fulsun.cleanup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pers.fulsun.cleanup.model.context.TaskContext;
import pers.fulsun.cleanup.model.dto.CleanupRequest;
import pers.fulsun.cleanup.service.executor.CleanupExecutor;

@Service
public class SseService {


    @Autowired
    private CleanupExecutor executor;
    @Autowired
    private LogService logService;

    // 创建并存储Emitter
    public SseEmitter createEmitter(String taskId) {
        return logService.createEmitter(taskId);
    }

    // 模拟异步查重任务
    @Async
    public void startImageCleanTask(String taskId, CleanupRequest request) {
        try {

            executor.execute(request.getOptions(), new TaskContext(taskId, request.getImageDirectory(), request.getBackupPath()));

            // 延迟一段时间再关闭连接
            try {
                Thread.sleep(500); // 延迟500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            logService.completeEmitter(taskId); // 关闭连接
        }
    }
}