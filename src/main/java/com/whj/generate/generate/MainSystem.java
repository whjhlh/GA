package com.whj.generate.generate;

import com.whj.generate.actual.QueryController;
import com.whj.generate.model.TestCase;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author whj
 * @date 2025-01-01 下午7:09
 */
public class MainSystem {
    public static void main(String[] args) throws NoSuchMethodException {
        ArrayList<TestCase> testCases = new ArrayList<>();
        Class<QueryController> queryControllerClass = QueryController.class;
        TestCase e = new TestCase(List.of(Mockito.class),"12", queryControllerClass.getMethod("query", String.class), "\"test\"");
        TestCase f = new TestCase(List.of(Mockito.class),"13", queryControllerClass.getMethod("query", String.class), "\"test\"");
        testCases.add(e);
        testCases.add(f);
        String testCode = new MockitoTestCodeGenerator().generateTestCode(queryControllerClass, testCases);
        System.out.println("生成的测试代码：\n" + testCode);
        // 可选择保存到文件
        //CodeSaverUtil.saveToFile("GeneratedTest.java", testCode);

    }
}
