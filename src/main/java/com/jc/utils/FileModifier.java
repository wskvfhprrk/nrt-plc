package com.jc.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.*;

public class FileModifier {

    /**
     * 修改指定文件中，指定参数名的值。
     * 
     * @param filePath 文件路径
     * @param parameter 需要修改的参数名（如 DATA_BEEF10）
     * @param value 新的值
     * @throws IOException 如果发生 IO 异常
     */
    public static void modifyParameterValueInFile(String filePath, String parameter, String value) throws IOException {
        // 创建文件对象
        File file = new File(filePath);

        // 检查文件是否存在
        if (!file.isFile()) {
            System.out.println("指定的路径不是有效的文件！");
            return;
        }

        // 读取文件内容
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        // 构建正则表达式动态匹配指定参数
        String regex = "(?<=set \"" + parameter + "=)(\\d+|\\S+)";  // 匹配数字或者非空字符
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            // 替换匹配到的值为新的值
            String modifiedContent = content.replaceAll("(?<=set \"" + parameter + "=)[^\"\r\n]+", value);

            // 将修改后的内容写回文件
            Files.write(file.toPath(), modifiedContent.getBytes(StandardCharsets.UTF_8));

            System.out.println("文件中的 " + parameter + " 参数值已更新为：" + value);
        } else {
            System.out.println("未找到 " + parameter + " 参数！");
        }
    }

    public static void main(String[] args) {
        try {
            // 调用公共方法，动态修改参数值
            modifyParameterValueInFile("d:/start_my_java_service.bat", "DATA_VIBRATOR_TIME", "5");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
