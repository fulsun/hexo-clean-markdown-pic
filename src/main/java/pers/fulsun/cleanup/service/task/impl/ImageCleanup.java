package pers.fulsun.cleanup.service.task.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import pers.fulsun.cleanup.exception.TaskException;
import pers.fulsun.cleanup.model.context.TaskContext;
import pers.fulsun.cleanup.model.enums.CleanupTaskType;
import pers.fulsun.cleanup.service.LogService;
import pers.fulsun.cleanup.service.task.CleanupTask;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(2)
public class ImageCleanup implements CleanupTask {

    @Autowired
    private LogService logService;
    @Autowired
    private ThreadPoolTaskExecutor cleanupTaskExecutor;
    private static final String IMAGE_REGEX = "!\\[.*?\\]\\((.*?)\\)";

    @Override
    public CleanupTaskType getTaskType() {
        return CleanupTaskType.CLEAN_DUPLICATE;
    }

    @Override
    public void execute(TaskContext context) throws TaskException {
        String taskId = context.getTaskId();
        String baseDir = context.getImageDirectory();
        // 解析markdown,提前文件中的图片
        File postDirFile = new File(baseDir);
        if (!postDirFile.exists()) {
            throw new TaskException("目录不存在");
        }
        // 遍历markdown文件
        List<File> markdownFiles = handleMarkdown(postDirFile, taskId);
        // 创建CountDownLatch，初始值为任务数量
        CountDownLatch latch = new CountDownLatch(markdownFiles.size());
        // 线程池提交任务
        for (File markdownFile : markdownFiles) {
            cleanupTaskExecutor.execute(() -> {
                try {
                    handleImage(markdownFile, taskId);
                } finally {
                    // 每个任务完成后，计数器减一
                    latch.countDown();
                }
            });
        }
        // 等待所有任务完成
        try {
            // 等待所有任务完成
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskException("任务执行被中断", e);
        }

    }

    private void handleImage(File markdownFile, String taskId) {
        logService.sendLog(taskId, "处理文件: " + markdownFile.getName());
        try {
            // 读取 Markdown 文件内容
            String markdownContent = readFileContent(markdownFile);

            // 提取图片链接
            List<String> imagePaths = extractImageUrls(markdownContent);

            String markdownFileName = markdownFile.getName().replace(".md", "");
            File imageDir = new File(markdownFile.getParent(), markdownFileName);
            File tempImageDir = new File(markdownFile.getParent(), "temp_" + markdownFileName);

            if (imagePaths.isEmpty()) {
                // 清理同名的空文件夹
                imageDir.delete();
                return;
            }

            if (imageDir.exists()) {
                // 移动到临时目录
                imageDir.renameTo(tempImageDir);
                // 创建与 Markdown 文件同名的目录
                imageDir.mkdirs();
            }

            boolean isUpdated = false;
            // 处理每张图片
            for (String imagePath : imagePaths) {
                File imageFile = resolveImageFile(tempImageDir, imagePath);
                if (imageFile != null && imageFile.exists()) {
                    // 计算图片的 MD5 值作为新文件名
                    String md5 = calculateMD5(imageFile);
                    File renameImageFile = new File(imageDir, md5 + "." + getFileExtension(imageFile.getName()));
                    // 移动或复制图片到目标目录
                    if (!renameImageFile.exists()) {
                        Files.copy(imageFile.toPath(), renameImageFile.toPath());
                    }
                    if (md5.equals(imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf(".")))) {
                        continue; // 图片名与 MD5 值相同，跳过
                    }
                    // 更新 Markdown 内容中的图片路径
                    markdownContent = markdownContent.replace(imagePath, markdownFileName + "/" + renameImageFile.getName());
                    isUpdated = true;
                } else {
                    logService.sendLog(taskId, "❌❌❌ " + markdownFileName + "=> 图片不存在: " + imagePath);
                }
            }

            // 清理临时目录
            if (tempImageDir.exists()) {
                for (File file : tempImageDir.listFiles()) {
                    file.delete();
                }
                tempImageDir.delete();
            }

            if (!isUpdated) {
                return;
            }
            // 将更新后的 Markdown 内容写回文件
            writeFileContent(markdownFile, markdownContent);
        } catch (Exception e) {
            logService.sendLog(taskId, e.getMessage());
        }

    }

    /**
     * 将内容写入文件
     */
    private void writeFileContent(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }


    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 计算文件的 MD5 值
     */
    private String calculateMD5(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] md5Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    /**
     * 解析图片路径为 File 对象
     */
    private File resolveImageFile(File imageDir, String imagePath) {
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            // 如果是网络图片，下载到临时目录
            try {
                URL url = new URL(imagePath);
                File tempFile = File.createTempFile("image", null);
                try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                return tempFile;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // 如果是本地图片，解析为绝对路径
            String filename = imagePath;
            if (imagePath.contains("/")) {
                filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            }
            return new File(imageDir, filename);
        }
    }

    private List<String> extractImageUrls(String markdownContent) {
        List<String> imageUrls = new ArrayList<>();
        Pattern pattern = Pattern.compile(IMAGE_REGEX);
        Matcher matcher = pattern.matcher(markdownContent);
        while (matcher.find()) {
            // 匹配到的图片链接
            imageUrls.add(matcher.group(1));
        }
        return imageUrls;
    }

    private void readImage(String imageUrl, String taskId) {
    }

    private String readFileContent(File markdownFile) throws IOException {
        //  return new String(Files.readAllBytes(file.toPath()));
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(markdownFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

        }
        return content.toString();
    }

    private List<File> handleMarkdown(File postDirFile, String taskId) {
        List<File> markdownFiles = new ArrayList<>();
        File[] files = postDirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".md")) {
                    markdownFiles.add(file);
                } else if (file.isDirectory()) {
                    markdownFiles.addAll(handleMarkdown(file, taskId));
                }
            }
        }
        return markdownFiles;
    }

    @Override
    public boolean shouldExecute(Set<Integer> options) {
        return true;
    }
}
