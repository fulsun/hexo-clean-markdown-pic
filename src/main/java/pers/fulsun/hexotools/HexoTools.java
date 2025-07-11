package pers.fulsun.hexotools;

import pers.fulsun.hexotools.common.Constant;
import pers.fulsun.hexotools.handle.HexoTimeUpdater;
import pers.fulsun.hexotools.handle.ImageRenamer;
import pers.fulsun.hexotools.handle.MarkdownDuplicateCleaner;
import pers.fulsun.hexotools.handle.MarkdownImageChecker;
import pers.fulsun.hexotools.handle.MarkdownImageFix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HexoTools {
    public static void main(String[] args) throws Exception {

        // 接受一个_post目录
        Scanner inputScanner = new Scanner(System.in);
        System.out.print("请输入_post目录: ");
        String inputDir = inputScanner.nextLine();
        String postsDirectory = inputDir == null || inputDir.trim().isEmpty() ? "_post" : inputDir;
        System.out.println("您输入的_post目录为: " + postsDirectory);

        // 检查目录是否存在
        File directory = new File(postsDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("错误：指定的目录不存在或不是一个有效的目录！");
            return;
        }

        // 打印菜单
        do {
            showMenu(inputScanner, postsDirectory);
        } while (true);
    }

    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                // Windows 使用 `cls`
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Linux/Mac 使用 `clear`
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (final Exception e) {
            // 如果清屏失败，至少打印 50 个空行模拟清屏
            System.out.println("\n".repeat(50));
        }
    }

    private static void showMenu(Scanner scanner, String mdDir) throws Exception {
        clearConsole(); // 先清屏
        System.out.println("========请选择要执行的功能============");
        System.out.println("1. 检测图片");
        System.out.println("2. 检测图片并从图库修复失效图片");
        System.out.println("3. 检测图片并修复并下载网络图片");
        System.out.println("4. 检测图片并修复并下载网络图片并格式化文档");
        System.out.println("5. 采用MD5重命名图片");
        System.out.println("6. Markdonw文件去重");
        System.out.println("7. hexo Formatter 引入时间");

        System.out.println("9. 退出");
        System.out.print("请输入您的选择: ");

        // 验证用户输入是否为有效整数
        while (!scanner.hasNextInt()) {
            System.out.println("无效的选择，请输入一个数字（1-5）!");
            System.out.print("请输入您的选择: ");
            scanner.next(); // 清除无效输入
        }

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                handleCheck(mdDir);
                break;
            case 2:
                handleCheckAndFix(mdDir);
                break;
            case 3:
                handleCheckAndFixAndDownload(mdDir);
                break;
            case 4:
                handleCheckAndFixAndDownloadAndFormat(mdDir);
                break;
            case 5:
                renameMdImageName(mdDir);
                break;
            case 6:
                markdownDuplicateCleaner(mdDir);
                break;
            case 7:
                hexoTimeUpdate(mdDir);
                break;
            case 9:
                System.out.println("退出程序");
                System.exit(0);
                break;
            default:
                System.out.println("无效的选择，请重新输入！");
        }
    }


    private static void hexoTimeUpdate(String postsDirectory) throws IOException {
        // 配置：Hexo 文章目录（通常是 /source/_posts）
        HexoTimeUpdater.processDirectory(Path.of(postsDirectory));
        System.out.println("\n处理完成！所有 Hexo 文章的创建/修改时间已更新。");
    }

    private static void markdownDuplicateCleaner(String postsDirectory) throws IOException, NoSuchAlgorithmException {
        System.out.println("正在检测重复的markdown...");
        // 调用 handle 包中的具体操作类
        MarkdownDuplicateCleaner.removeDuplicate(postsDirectory);
        // 此处可添加相应的调用逻辑
        System.out.println("操作完成。");
        waitForUserInput();
    }

    private static void renameMdImageName(String postsDirectory) {
        ImageRenamer imageRenamer = new ImageRenamer();
        Map<String, List<String>> imageRenameMap = imageRenamer.generateRenameMap(postsDirectory);
        if (!imageRenameMap.isEmpty()) {
            imageRenamer.applyRename(imageRenameMap);
        }
    }

    private static void handleCheckAndFix(String postsDirectory) {
        System.out.println("正在检测图片并修复...");
        // 调用 handle 包中的具体操作类
        new MarkdownImageFix().fix(postsDirectory);
        // 此处可添加相应的调用逻辑
        System.out.println("操作完成。");
        waitForUserInput();
    }

    private static void handleCheckAndFixAndDownload(String mdDir) {
        System.out.println("正在检测图片并修复，并下载网络图片...");
        // 调用 handle 包中的具体操作类
        // 此处可添加相应的调用逻辑
        System.out.println("操作完成。");
        waitForUserInput();
    }

    private static void handleCheckAndFixAndDownloadAndFormat(String mdDir) {
        System.out.println("正在检测图片并修复，下载网络图片，并格式化文档...");
        // 调用 handle 包中的具体操作类
        // 此处可添加相应的调用逻辑
        System.out.println("操作完成。");
        waitForUserInput();
    }

    private static void handleCheck(String postsDirectory) {
        System.out.println("开始检测图片...");
        Map<String, Map<String, List<String>>> checkResult = new MarkdownImageChecker().check(Path.of(postsDirectory));
        MarkdownImageChecker.printCheckResult(checkResult.get(Constant.INVALID_IMAGES), checkResult.get(Constant.INVALID_REMOTE_IMAGES), checkResult.get(Constant.USED_IMAGES));
        System.out.println("图片检测完成。");
        waitForUserInput();
    }


    private static void waitForUserInput() {
        System.out.println("按回车键继续...");
        new Scanner(System.in).nextLine(); // 等待用户按下回车
    }

}
