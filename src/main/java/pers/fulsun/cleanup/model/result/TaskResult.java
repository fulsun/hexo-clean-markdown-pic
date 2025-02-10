package pers.fulsun.cleanup.model.result;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskResult {
    String taskName;
    boolean success;
    String message;
}

