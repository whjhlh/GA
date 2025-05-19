package com.whj.generate.common.service;

import com.whj.generate.common.dto.ClassInfoDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author whj
 * @date 2025-05-19 下午5:42
 */
public interface JavaStructureService {

    List<ClassInfoDTO> scanJavaFiles() throws IOException;

    List<Map<String, Object>> extractMethodCodeWithLineNumbers(String className, String methodName) throws IOException;


    Set<Integer> cantNotCoveredLines(String className, String methodName) ;
}
