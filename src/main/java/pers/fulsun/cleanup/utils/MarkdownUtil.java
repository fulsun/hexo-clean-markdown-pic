package pers.fulsun.cleanup.utils;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.regex.Pattern;

public class MarkdownUtil {
    // 正则表达式模式，用于匹配中英文相邻的情况
    private static final Pattern CHINESE_ENGLISH_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5])([a-zA-Z])");
    private static final Pattern ENGLISH_CHINESE_PATTERN = Pattern.compile("([a-zA-Z])([\\u4e00-\\u9fa5])");
    // 在中英文之间添加空格的方法
    private static String addSpacesBetweenChineseAndEnglish(String text) {
        // 处理中文后跟英文的情况
        text = CHINESE_ENGLISH_PATTERN.matcher(text).replaceAll("$1 $2");
        // 处理英文后跟中文的情况
        text = ENGLISH_CHINESE_PATTERN.matcher(text).replaceAll("$1 $2");
        return text;
    }
    // 格式化 Markdown 文本的方法
    public static String formatMarkdown(String markdown) {
        // 创建 Flexmark 解析器的配置
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        // 解析 Markdown 文本为节点树
        Node document = parser.parse(markdown);
        // 将节点树转换为 Markdown 文本
        String parsedMarkdown = document.toString();
        // 在中英文之间添加空格
        return addSpacesBetweenChineseAndEnglish(parsedMarkdown);
    }

    public static String markdownToHtml(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
