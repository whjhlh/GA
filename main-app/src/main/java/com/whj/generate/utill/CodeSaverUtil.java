package com.whj.generate.utill;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author whj
 * @date 2025-01-01 下午9:53
 */
public class CodeSaverUtil {
    public static void saveToFile(String fileName, String content) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
            System.out.println("测试代码已保存到文件：" + fileName);
        } catch (IOException e) {
            System.out.println("保存文件时发生错误：" + e.getMessage());
        }
    }
}
