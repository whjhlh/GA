package com.whj.generate.model;

import java.util.List;

/**
 *
 * @author whj
 * @date 2025-01-01 下午9:51
 */
public class MethodStructure {
    // 方法名
    private String name;
    // 参数
    private List<Class<?>> parameters;

    public MethodStructure(String name, List<Class<?>> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public List<Class<?>> getParameters() {
        return parameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(List<Class<?>> parameters) {
        this.parameters = parameters;
    }

}

