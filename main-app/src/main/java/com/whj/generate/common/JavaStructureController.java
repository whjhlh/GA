package com.whj.generate.common;

import com.whj.generate.common.dto.ClassInfoDTO;
import com.whj.generate.common.service.JavaStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * Updated to support methods with additional modifiers (e.g. static, final).
 */
@RestController
@RequestMapping("/api/java-structure")
public class JavaStructureController {

    private final JavaStructureService service;

    @Autowired
    public JavaStructureController(JavaStructureService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClassInfoDTO> getJavaStructure() throws IOException {
        return service.scanJavaFiles();
    }
}
