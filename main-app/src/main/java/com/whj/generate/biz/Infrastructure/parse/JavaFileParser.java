package com.whj.generate.biz.Infrastructure.parse;

import com.whj.generate.common.dto.ClassInfoDTO;
import com.whj.generate.common.dto.MethodInfoDTO;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author whj
 * @date 2025-05-05 下午10:54
 */
public final class JavaFileParser {

    private static final Pattern CLASS_PATTERN =
            Pattern.compile("public\\s+(?:class|interface|enum)\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN =
            Pattern.compile("public\\s+(?:\\w+\\s+)*?(\\w+(?:<[^>]+>)?)\\s+(\\w+)\\s*\\(");

    private JavaFileParser() { /* Utility class */ }

    public static Optional<ClassInfoDTO> parse(String content) {
        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (!classMatcher.find()) {
            return Optional.empty();
        }
        String className = classMatcher.group(1);
        ClassInfoDTO info = new ClassInfoDTO(className);

        Matcher methodMatcher = METHOD_PATTERN.matcher(content);
        while (methodMatcher.find()) {
            String returnType = methodMatcher.group(1);
            String methodName = methodMatcher.group(2);
            info.addMethod(new MethodInfoDTO(methodName, returnType));
        }
        return Optional.of(info);
    }
}

