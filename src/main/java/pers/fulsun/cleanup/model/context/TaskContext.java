package pers.fulsun.cleanup.model.context;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskContext {
    private String taskId;
    private String imageDirectory;
    private String backupPath;

}
