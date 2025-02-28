package com.whj.generate.dualPipeline;

import com.whj.generate.model.Chromosome;
import com.whj.generate.utill.ProxyUtil;
import com.whj.generate.utill.ReflectionUtil;
import com.whj.generate.utill.StringUtil;

import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-03-29 下午3:55
 */
public class test {
    public static void main(String[] args) throws InterruptedException {
//        geneticsTaskProcess geneticsTaskProcess = new geneticsTaskProcess();
//        geneticsTaskProcess.start();
//        Thread.sleep(5000);
//        System.out.println(JsonUtil.toJson(geneticsTaskProcess.getSuccessList()));
        testForCover realInstance = new testForCover();
        Object coverageProxy = ProxyUtil.createCoverageProxy(realInstance);
        Class<? extends testForCover> aClass = realInstance.getClass();
        for(Method method :aClass.getMethods()){
            if(StringUtil.equals(method.getName(),"test")){
                Chromosome chromosome=new Chromosome(method);
                chromosome.setMethod(method);
                chromosome.setGenes(new Object[]{1,2,3});
                ReflectionUtil.invokeMethod(coverageProxy,method,chromosome.getGenes());
            }
        }

    }
}
