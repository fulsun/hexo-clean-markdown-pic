package pers.fulsun.cleanup.model.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@AllArgsConstructor
@Data
public class CleanupResult {
    List<TaskResult> taskResults;
}
