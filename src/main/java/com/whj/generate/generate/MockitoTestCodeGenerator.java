package com.whj.generate.generate;

import com.whj.generate.model.TestCase;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 测试代码生成器
 *
 * @author whj
 * @date 2025-01-01 下午7:09
 */
public class MockitoTestCodeGenerator {
    // 生成测试文件
    public String generateTestCode(Class<?> clazz, List<TestCase> testCases) {
        StringBuilder testClassContent = new StringBuilder();
        // 添加类声明
        Set<Class<?>> uniqueImportClazzList = testCases.stream()
                .flatMap(testCase -> testCase.getImportClazzList().stream()) // 展开所有内部列表元素
                .collect(Collectors.toSet());
        testClassContent.append(generateImportClazzCode(uniqueImportClazzList));
        testClassContent.append(generateClassDeclaration(clazz));
        // 遍历外部提供的用例
        for (TestCase testCase : testCases) {
            generateCaseCode(testClassContent, testCase);
        }
        // 结束类声明
        testClassContent.append("}");
        return testClassContent.toString();
    }

    /**
     * 生成测试用例代码
     *
     * @param testClassContent 测试类内容
     * @param testCase         测试用例模型
     */
    private void generateCaseCode(StringBuilder testClassContent, TestCase testCase) {
        testClassContent.append(generateMethodDeclaration(testCase.getTestName()));
        testClassContent.append(testCase.getBody());
        testClassContent.append("\n    }\n");
    }

    /**
     * 生成引入路径代码
     */
    private String generateImportClazzCode(Set<Class<?>> importClazzList) {
        StringBuilder importClazzContent = new StringBuilder();
        for (Class<?> clazz : importClazzList) {
            importClazzContent.append(String.format("import %s;\n", clazz.getName()));
        }
        return importClazzContent.toString();
    }

    /**
     * 生成类声明
     *
     * @param clazz
     * @return
     */
    private String generateClassDeclaration(Class<?> clazz) {
        return String.format(
                "public class %sTest {\n", clazz.getSimpleName());
    }

    /**
     * 生成测试方法声明
     *
     * @param methodName
     * @return
     */
    private String generateMethodDeclaration(String methodName) {
        return String.format("   @Test\n" +
                "   public void test_%s() {\n", StringUtils.capitalize(methodName));
    }
}
