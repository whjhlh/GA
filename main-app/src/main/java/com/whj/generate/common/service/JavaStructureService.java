package com.whj.generate.common.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.whj.generate.biz.Infrastructure.parse.JavaFileParser;
import com.whj.generate.common.dto.ClassInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.whj.generate.utill.PathUtils.getFilePath;

/**
 * @author whj
 * @date 2025-05-05 下午10:53
 */
@Service
public class JavaStructureService {

    private static final Logger logger = LoggerFactory.getLogger(JavaStructureService.class);
    private static final String ROOT_PATH = "/Users/b20210304129/Desktop/school/biyesheji/generatetestclass/main-app/src/main/java/com/whj/generate/whjtest";

    public List<ClassInfoDTO> scanJavaFiles() throws IOException {
        List<ClassInfoDTO> result = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(ROOT_PATH))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> processFile(path, result));
        }
        return result;
    }

    private void processFile(Path path, List<ClassInfoDTO> result) {
        try {
            String content = Files.readString(path);
            Optional<ClassInfoDTO> parse = JavaFileParser.parse(content);
            parse.ifPresent(result::add);
        } catch (IOException e) {
            logger.error("Failed to read file {}", path, e);
        }
    }

    public List<Map<String, Object>> extractMethodCodeWithLineNumbers(String className, String methodName) throws IOException {
        CompilationUnit cu = parseClass(className);
        List<Map<String, Object>> result = new ArrayList<>();

        cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> m.getNameAsString().equals(methodName))
                .findFirst()
                .ifPresent(method -> {
                    if (method.getRange().isPresent()) {
                        int start = method.getRange().get().begin.line;
                        int end = method.getRange().get().end.line;
                        List<String> lines = getSourceLines(className); // 读取整个文件为 List<String>

                        for (int i = start; i <= end; i++) {
                            Map<String, Object> lineMap = new HashMap<>();
                            lineMap.put("lineNumber", i);
                            lineMap.put("content", lines.get(i - 1)); // 下标从0开始
                            result.add(lineMap);
                        }
                    }
                });

        return result;
    }

    // 解析 className 对应的 CompilationUnit
    private CompilationUnit parseClass(String className) throws IOException {
        String path = resolveSourceFilePath(className);
        File file = new File(path);
        return StaticJavaParser.parse(file);
    }

    // 读取源文件所有行
    private List<String> getSourceLines(String className) {
        String path = resolveSourceFilePath(className);
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 解析类名为源代码路径，例如：com.whj.demo.ExampleClass -> src/main/java/com/whj/demo/ExampleClass.java
    private String resolveSourceFilePath(String className) {
        try {
            Class<?> targetClass = Class.forName("com.whj.generate.whjtest." + className);
            String filePath = getFilePath(targetClass, StandardCharsets.UTF_8);
            return filePath;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


}
