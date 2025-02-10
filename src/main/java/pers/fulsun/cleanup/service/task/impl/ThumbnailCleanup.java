package pers.fulsun.cleanup.service.task.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pers.fulsun.cleanup.model.context.TaskContext;
import pers.fulsun.cleanup.model.enums.CleanupTaskType;
import pers.fulsun.cleanup.service.task.CleanupTask;

/**
 * 清理缩略图任务
 * @author fulsun
 */
@Component
@Order(3)
public class ThumbnailCleanup implements CleanupTask {

    @Override
    public CleanupTaskType getTaskType() {
        return CleanupTaskType.CLEAN_THUMBNAILS;
    }


    @Override
    public void execute(TaskContext context) {
        // 清理缩略图逻辑
    }
}