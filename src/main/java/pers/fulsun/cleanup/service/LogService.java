package pers.fulsun.cleanup.service;


import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class LogService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<String>> logCache = new ConcurrentHashMap<>(); // 缓存日志
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>(); // 记录心跳任务

    // 创建并存储Emitter
    public SseEmitter createEmitter(String taskId) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter == null) {
            // 创建Emitter
            emitter = new SseEmitter(8_000L); // 设置超时时间
            emitters.put(taskId, emitter);

            // 设置回调以清理资源
            emitter.onCompletion(() -> {
                completeEmitter(taskId);
                emitters.remove(taskId);
                logCache.remove(taskId); // 清理缓存
            });
            emitter.onTimeout(() -> {
                completeEmitter(taskId);
                emitters.remove(taskId);
                logCache.remove(taskId); // 清理缓存
            });
            emitter.onError((e) -> {
                e.printStackTrace();
                completeEmitter(taskId);
                emitters.remove(taskId);
                logCache.remove(taskId); // 清理缓存
            });

            // 发送缓存的日志
            if (logCache.containsKey(taskId)) {
                List<String> logs = logCache.get(taskId);
                logs.forEach(log -> sendLog(taskId, log));
            }

            // 发送心跳包
            ScheduledFuture<?> heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
                System.out.println(Thread.currentThread().getName() + "\t" + taskId + "\tsend heartbeat");
                SseEmitter taskEmitter = emitters.get(taskId);
                if (taskEmitter != null) {
                    try {
                        taskEmitter.send(SseEmitter.event().comment("heartbeat"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 5, TimeUnit.SECONDS);
            heartbeatTasks.put(taskId, heartbeatTask); // 记录心跳任务
        }
        return emitter;
    }

    // 发送日志消息
    public void sendLog(String taskId, String log) {
        // 缓存日志
        logCache.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>()).add(log);
        // 发送日志到客户端
        SseEmitter emitter = emitters.get(taskId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(log));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(taskId);
                logCache.remove(taskId); // 清理缓存
            }
        }
    }


    /**
     * 主动关闭Emitter
     * 服务器会主动关闭与客户端的连接。此时，EventSource 会检测到连接中断，并触发 onerror 事件。
     *
     * @param taskId
     */
    public void completeEmitter(String taskId) {
        SseEmitter emitter = emitters.get(taskId);
        cancelHeartbeatTask(taskId);
        if (emitter != null) {
            emitter.complete();
            emitters.remove(taskId);
            logCache.remove(taskId); // 清理缓存
        }
    }

    // 取消心跳任务
    private void cancelHeartbeatTask(String taskId) {
        ScheduledFuture<?> heartbeatTask = heartbeatTasks.get(taskId);
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
            heartbeatTasks.remove(taskId);
        }
    }
}
