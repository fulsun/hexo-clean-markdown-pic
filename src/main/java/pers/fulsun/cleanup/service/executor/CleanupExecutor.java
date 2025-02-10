package pers.fulsun.cleanup.service.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.fulsun.cleanup.exception.TaskException;
import pers.fulsun.cleanup.model.context.TaskContext;
import pers.fulsun.cleanup.model.result.CleanupResult;
import pers.fulsun.cleanup.model.result.TaskResult;
import pers.fulsun.cleanup.service.LogService;
import pers.fulsun.cleanup.service.task.CleanupTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CleanupExecutor {
    private final List<CleanupTask> allTasks;
    private final LogService logService;

    @Autowired
    public CleanupExecutor(List<CleanupTask> allTasks, LogService logService) {
        this.allTasks = allTasks;
        this.logService = logService;
    }

    public CleanupResult execute(Set<Integer> options, TaskContext context) {
        String taskId = context.getTaskId();
        logService.sendLog(taskId, "👇👇👇👇👇👇👇👇👇👇👇👇");
        logService.sendLog(taskId, taskId + "开始执行任务！");
        List<TaskResult> results = new ArrayList<>();

        for (CleanupTask task : allTasks) {
            if (task.shouldExecute(options)) {

                try {
                    logService.sendLog(taskId, "开始执行任务: " + task.getTaskType().getDescription());
                    task.execute(context);
                    results.add(new TaskResult(task.getTaskType().name(), true, "Success"));
                    logService.sendLog(taskId, "任务完成: " + task.getTaskType().getDescription());
                } catch (TaskException e) {
                    results.add(new TaskResult(task.getTaskType().name(), false, e.getMessage()));
                    logService.sendLog(taskId, "任务失败: " + e.getMessage());
                }
            }
        }
        logService.sendLog(taskId, "👆👆👆👆👆👆👆👆👆👆👆👆");
        logService.sendLog(taskId, taskId + "全部任务完成！");
        return new CleanupResult(results);
    }
}
