package pers.fulsun.cleanup.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CleanupRequest {
    private Set<Integer> options;
    private String imageDirectory;
    private String backupPath;
}