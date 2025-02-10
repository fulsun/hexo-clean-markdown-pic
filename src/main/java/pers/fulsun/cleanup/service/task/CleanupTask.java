package pers.fulsun.cleanup.service.task;

import pers.fulsun.cleanup.exception.TaskException;
import pers.fulsun.cleanup.model.context.TaskContext;
import pers.fulsun.cleanup.model.enums.CleanupTaskType;

import java.util.Set;

public interface CleanupTask {
    CleanupTaskType getTaskType();
    void execute(TaskContext context) throws TaskException;

    /**
     * 判断任务是否需要执行 默认实现（Java 8+）
     *
     * @param options 前端传递的任务选项
     * @return 是否需要执行
     */
    default boolean shouldExecute(Set<Integer> options) {
        return options.contains(getTaskType().getCode());
    }
}
