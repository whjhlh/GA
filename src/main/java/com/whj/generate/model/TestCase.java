package com.whj.generate.model;

/**
 * @author whj
 * @date 2025-01-01 下午9:47
 */
import java.lang.reflect.Method;
import java.util.List;

// 用例描述类
public class TestCase {
    /**
     * 用例涉及到的clazz信息
     */
    private final List<Class<?>> importClazzList;
    private final String testName; // 用例名称
    private final Method method;   // 目标方法
    private final String body;     // 用例逻辑

    public TestCase(List<Class<?>> importClazzList, String testName, Method method, String body) {
        this.importClazzList = importClazzList;
        this.testName = testName;
        this.method = method;
        this.body = body;
    }

    public List<Class<?>> getImportClazzList() {
        return importClazzList;
    }

    public String getTestName() {
        return testName;
    }

    public Method getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }
}
