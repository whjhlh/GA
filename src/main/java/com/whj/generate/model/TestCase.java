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
    private  List<Class<?>> importClazzList;
    /**
     * 用例名称
     */
    private  String testName;
    /**
     * 目标方法
     */
    private  Method method;
    /**
     * 用例逻辑
     */
    private  String body;

    public void setImportClazzList(List<Class<?>> importClazzList) {
        this.importClazzList = importClazzList;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setBody(String body) {
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
