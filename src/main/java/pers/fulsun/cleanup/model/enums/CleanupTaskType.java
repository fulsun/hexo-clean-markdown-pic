package pers.fulsun.cleanup.model.enums;

import java.util.Arrays;

public enum CleanupTaskType {
    CLEAN_DUPLICATE(0, "清理重复文件任务"),
    BACKUP(1, "备份任务"),
    CLEAN_EMPTY(2, "清理空文件任务"),
    CLEAN_THUMBNAILS(3, "清理缩略图任务"),
    OTHER(4, "其他任务");

    private final int code;
    private final String description;

    CleanupTaskType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CleanupTaskType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的任务代码: " + code));
    }
}
