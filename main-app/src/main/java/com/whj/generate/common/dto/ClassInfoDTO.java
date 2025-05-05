package com.whj.generate.common.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author whj
 * @date 2025-05-05 下午10:55
 */

public class ClassInfoDTO {
    /**
     * 类名
     */
    private final String className;
    /**
     * 方法列表
     */
    private final List<MethodInfoDTO> methods;

    public ClassInfoDTO(String className) {
        this.className = className;
        methods = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public List<MethodInfoDTO> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public void addMethod(MethodInfoDTO method) {
        methods.add(method);
    }
}
