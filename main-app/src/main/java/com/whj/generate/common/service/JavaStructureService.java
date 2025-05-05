package com.whj.generate.common.service;

import com.whj.generate.biz.Infrastructure.parse.JavaFileParser;
import com.whj.generate.common.dto.ClassInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
}
