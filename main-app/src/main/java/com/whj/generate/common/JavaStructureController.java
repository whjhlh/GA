package com.whj.generate.common;

import com.whj.generate.common.dto.ClassInfoDTO;
import com.whj.generate.common.service.JavaStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Updated to support methods with additional modifiers (e.g. static, final).
 */
@RestController
@RequestMapping("/api/java-structure")
public class JavaStructureController {

    private final JavaStructureService javaStructureService;

    @Autowired
    public JavaStructureController(JavaStructureService javaStructureService) {
        this.javaStructureService = javaStructureService;
    }

    @GetMapping
    public List<ClassInfoDTO> getJavaStructure() throws IOException {
        return javaStructureService.scanJavaFiles();
    }
    @GetMapping("/method-code")
    public Map<String, Object> getMethodCode(String className, String methodName) throws IOException {
        List<Map<String, Object>> codeLines = javaStructureService.extractMethodCodeWithLineNumbers(className, methodName);
        Map<String, Object> result = new HashMap<>();
        result.put("codeLines", codeLines);
        return result;
    }


}
