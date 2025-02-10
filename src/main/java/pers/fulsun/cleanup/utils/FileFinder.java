package pers.fulsun.cleanup.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class FileFinder {
    /**
     * 使用广度优先搜索（BFS）查找文件
     *
     * @param directory 要查找的目录
     * @param filename  要查找的文件名
     * @return 找到的文件，如果未找到则返回 null
     */
    public static File findFileByNameBFS(File directory, String filename) {
        if (!directory.exists() || !directory.isDirectory()) {
            return null; // 如果目录不存在或不是目录，返回 null
        }

        // 使用队列实现 BFS
        Queue<File> queue = new LinkedList<>();
        queue.offer(directory); // 将初始目录加入队列

        while (!queue.isEmpty()) {
            File currentDir = queue.poll(); // 取出当前目录

            // 遍历当前目录下的所有文件和子目录
            File[] files = currentDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        queue.offer(file); // 如果是子目录，加入队列
                    } else if (file.getName().equalsIgnoreCase(filename)) {
                        return file; // 如果找到匹配的文件，返回该文件
                    }
                }
            }
        }

        return null; // 如果未找到，返回 null
    }


}
