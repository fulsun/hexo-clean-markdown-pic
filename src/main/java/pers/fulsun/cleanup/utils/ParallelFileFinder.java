package pers.fulsun.cleanup.utils;

import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ParallelFileFinder {
    public static void main(String[] args) {
        String filename = "targetFile.txt"; // 要查找的文件名
        File backupDir = new File("C:\\Users\\sfuli\\Desktop\\11\\"); // 备份目录

        // 创建 ForkJoinPool
        ForkJoinPool pool = new ForkJoinPool();
        // 提交任务并获取结果
        File foundFile = pool.invoke(new FileSearchTask(backupDir, filename));

        if (foundFile != null) {
            System.out.println("文件找到: " + foundFile.getAbsolutePath());
        } else {
            System.out.println("文件未找到");
        }
    }

    /**
     * 并行文件查找任务
     */
    static class FileSearchTask extends RecursiveTask<File> {
        private final File directory;
        private final String filename;

        public FileSearchTask(File directory, String filename) {
            this.directory = directory;
            this.filename = filename;
        }

        @Override
        protected File compute() {
            // 如果目录不存在或不是目录，返回 null
            if (!directory.exists() || !directory.isDirectory()) {
                return null;
            }

            // 遍历当前目录下的所有文件和子目录
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果是子目录，创建一个新的子任务
                        FileSearchTask subTask = new FileSearchTask(file, filename);
                        subTask.fork(); // 异步执行子任务
                        File result = subTask.join(); // 等待子任务完成并获取结果
                        if (result != null) {
                            return result; // 如果子任务找到文件，返回该文件
                        }
                    } else if (file.getName().equalsIgnoreCase(filename)) {
                        return file; // 如果找到匹配的文件，返回该文件
                    }
                }
            }

            return null; // 如果未找到，返回 null
        }
    }
}
