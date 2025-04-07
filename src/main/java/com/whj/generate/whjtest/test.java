package com.whj.generate.whjtest;

import com.whj.generate.condition.ParamThresholdExtractor2;
import com.whj.generate.utill.JsonUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author whj
 * @date 2025-03-29 下午3:55
 */
public class test {
    public static void main(String[] args) throws InterruptedException, IOException {
        long l2 = System.currentTimeMillis();
        Map<String, Set<Object>> test3 = ParamThresholdExtractor2.genGenetic(testForCover.class, "test");
        System.out.println(JsonUtil.toJson(test3));

        System.out.println("耗时：" + (System.currentTimeMillis() - l2));

    }


//        geneticsTaskProcess geneticsTaskProcess = new geneticsTaskProcess();
//        geneticsTaskProcess.start();
//        Thread.sleep(5000);
//        System.out.println(JsonUtil.toJson(geneticsTaskProcess.getSuccessList()));

//        Object coverageProxy = ProxyUtil.createCoverageProxy(realInstance);
//        Class<? extends testForCover> aClass = realInstance.getClass();
//        for(Method method :aClass.getMethods()){
//            if(StringUtil.equals(method.getName(),"test")){
//                Chromosome chromosome=new Chromosome(method);
////                chromosome.setMethod(method);
////                chromosome.setGenes(new Object[]{1,2,3});
////                ReflectionUtil.invokeMethod(coverageProxy,method,chromosome.getGenes());
////            }
////        }
//        // 2. 开始记录覆盖率
//        JaCocoUtil.startRecording();
//        ExecutionDataStore before = JaCocoUtil.getCurrentRecording(); // 初始化基准数据
//
//        Object result;
//        try {
//            realInstance.test(1,2,4);
//        } finally {
//            // 4. 确保异常时仍计算覆盖率
//            ExecutionDataStore after = JaCocoUtil.stopRecording();
//            Class<? extends testForCover> aClass1 = realInstance.getClass();
//            double coverage = JaCocoUtil.analyzeCoverage(aClass1, before, after);
//            System.out.println(coverage);
//        }
//        JaCocoUtil.cleanRecord();
}
