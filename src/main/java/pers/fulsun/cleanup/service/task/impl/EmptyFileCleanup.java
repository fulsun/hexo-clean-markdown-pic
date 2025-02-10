package pers.fulsun.cleanup.service.task.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pers.fulsun.cleanup.model.context.TaskContext;
import pers.fulsun.cleanup.model.enums.CleanupTaskType;
import pers.fulsun.cleanup.service.task.CleanupTask;

/**
 * 清理空文件任务
 * @author fsun
 */
@Component
@Order(5)
public class EmptyFileCleanup implements CleanupTask {
    @Override
    public CleanupTaskType getTaskType() {
        return CleanupTaskType.CLEAN_EMPTY;
    }

    @Override
    public void execute(TaskContext context) {
        // 清理空文件逻辑
    }
}
