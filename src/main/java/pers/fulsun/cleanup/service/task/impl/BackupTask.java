package pers.fulsun.cleanup.service.task.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pers.fulsun.cleanup.exception.TaskException;
import pers.fulsun.cleanup.model.context.TaskContext;
import pers.fulsun.cleanup.model.enums.CleanupTaskType;
import pers.fulsun.cleanup.service.LogService;
import pers.fulsun.cleanup.service.task.CleanupTask;
import pers.fulsun.cleanup.utils.JacksonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 备份任务
 *
 * @author fulsun
 */
@Component
@Order(1)
public class BackupTask implements CleanupTask {

    private final LogService logService;
    private volatile boolean isCancelled = false;

    @Autowired
    public BackupTask(LogService logService) {
        this.logService = logService;
    }

    @Override
    public CleanupTaskType getTaskType() {
        return CleanupTaskType.BACKUP;
    }


    @Override
    public void execute(TaskContext context) throws TaskException {
        String taskId = context.getTaskId();
        Path sourcePath = Paths.get(context.getImageDirectory());
        Path targetPath = createBackupDirectory(context.getBackupPath());

        try {
            copyDirectory(sourcePath, targetPath);
            logProgress(taskId, "文章备份完成到: " + targetPath);
        } catch (IOException e) {
            throw new TaskException("备份失败: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new TaskException("复制过程中发生错误: " + e.getMessage(), e);
        }
    }


    private void logProgress(String taskId, String message) {
        Map<String, String> log = new HashMap<>();
        log.put("timestamp", LocalDateTime.now().toString());
        log.put("taskId", taskId);
        log.put("message", message);
        logService.sendLog(taskId, JacksonUtil.toJson(log));
    }

    private void copyDirectory(Path sourcePath, Path targetPath) throws IOException {
        Files.walk(sourcePath).forEach(source -> {
            if (isCancelled) {
                throw new RuntimeException("任务被中断");
            }
            try {
                Path target = targetPath.resolve(sourcePath.relativize(source));
                if (Files.isDirectory(source)) {
                    if (!Files.exists(target)) {
                        Files.createDirectories(target);
                    }
                } else {
                    copyFile(source, target);
                }
            } catch (IOException e) {
                throw new RuntimeException("复制失败: " + source, e);
            }
        });
    }

    private void copyFile(Path source, Path target) throws IOException {
        try (InputStream in = Files.newInputStream(source); OutputStream out = Files.newOutputStream(target)) {
            byte[] buffer = new byte[8192]; // 8KB 缓冲区
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private Path createBackupDirectory(String backupPath) {
        Path targetPath = Paths.get(backupPath, System.currentTimeMillis() + "");
        try {
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }
            return targetPath;
        } catch (IOException e) {
            throw new TaskException("无法创建备份目录: " + targetPath, e);
        }
    }
}
