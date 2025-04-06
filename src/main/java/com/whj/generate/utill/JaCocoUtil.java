package com.whj.generate.utill;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.springframework.util.ClassUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;


/**
 * 线程安全的 JaCoCo 覆盖率工具类
 *
 * @author whj
 * @date 2025-02-22 下午7:47
 */
public class JaCocoUtil {
    private static final IRuntime runtime = new LoggerRuntime();
    private static final RuntimeData runtimeData = new RuntimeData();
    // 线程隔离的覆盖率存储
    private static final ThreadLocal<ExecutionDataStore> threadCoverage = ThreadLocal.withInitial(()->{
        ExecutionDataStore store = new ExecutionDataStore();
        return store;
    });
    static SessionInfo defaultSession;

    static {
        try {
            // 初始化 Session 信息
            defaultSession = new SessionInfo(
                    "coverage-session-" + System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
            );
            runtimeData.setSessionId(defaultSession.getId());
            runtime.startup(runtimeData);
            System.out.println("[JaCoCo] 运行时已启动，Session ID: " + defaultSession.getId());
        } catch (Exception e) {
            throw new RuntimeException("JaCoCo 初始化失败", e);
        }
    }


    /**
     * 分析覆盖率
     *
     * @param clazz
     * @param before
     * @param after
     * @return
     * @throws IOException
     */
    public static double analyzeCoverage(Class<?> clazz, ExecutionDataStore before, ExecutionDataStore after) throws IOException {
        //合并数据
        ExecutionDataStore merged = mergeStores(before, after);
        // 1. 路径处理
        final String decodedPath = PathUtils.getClassFilePath(clazz);
        final String resource = PathUtils.toClassResourcePath(clazz).replace(".class", "");
        CoverageBuilder builder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(merged, builder);
        System.out.println("[JaCoCo] 开始分析类: " +decodedPath+"   \n"+resource);
        analyzer.analyzeClass(new FileInputStream(decodedPath),resource);

        String expectedName = getOriginalClassName(clazz).replace('.', '/');
        for (IClassCoverage cc : builder.getClasses()) {
            for(IMethodCoverage mc : cc.getMethods()){
                if(mc.getName().equals("test")){
                    System.out.println("[JaCoCo] 找到方法: "+mc.getName()+mc.getLineCounter().getCoveredCount()*100);
                    break;
                }
            }
            if (cc.getName().equals(expectedName)) {
                int covered = cc.getLineCounter().getCoveredCount();
                int missed = cc.getLineCounter().getMissedCount();
                return covered * 100.0 / (covered + missed);
            }
        }
        throw new IllegalArgumentException("类未找到: " + clazz.getName());
    }

    /**
     * 获取当前线程的实时记录
     */
    public static ExecutionDataStore getCurrentRecording() {
        return threadCoverage.get();
    }

    public static void startRecording() {
        ExecutionDataStore store = threadCoverage.get();
        if (store == null) {
            store = new ExecutionDataStore();
            threadCoverage.set(store);
        }
        ISessionInfoVisitor visitor = info -> {
        }; // 空实现避免 NPE
        runtimeData.collect(store, visitor, true);
    }

    public static ExecutionDataStore stopRecording() {
        ExecutionDataStore store = threadCoverage.get();
        return store;
    }

    /**
     * 合并两个 ExecutionDataStore
     *
     * @param before
     * @param after
     * @return
     */
    private static ExecutionDataStore mergeStores(ExecutionDataStore before, ExecutionDataStore after) {
        System.out.println("[JaCoCo] 合并前数据条数: " + before.getContents().size());
        System.out.println("[JaCoCo] 合并后数据条数: " + after.getContents().size());
        ExecutionDataStore merged = new ExecutionDataStore();
        mergeData(before, merged);
        mergeData(after, merged);
        return merged;
    }

    private static void mergeData(ExecutionDataStore store, ExecutionDataStore merged) {
        for (ExecutionData data : store.getContents()) {
            ExecutionData executionData = merged.get(data.getId());
            if (executionData == null) {
                merged.put(data);
            } else {
                executionData.merge(data);
            }
        }
    }

    /**
     * 获取原始类名
     * @param clazz
     * @return
     */
    private static String getOriginalClassName(Class<?> clazz) {
        String name = clazz.getName();
        if(name.contains("$$EnhancerByCGLIB$$")){
            return name.substring(0, name.indexOf("$$EnhancerByCGLIB$$"));
        }else if(name.contains("$Proxy")){
            return clazz.getSuperclass().getName();
        }
        return name;
    }


    /**
     * 关闭 JaCoCo 运行时
     */
    public static void shutdown() {
        runtime.shutdown();
    }

    /**
     * 记录覆盖率日志
     */
    public static void logCoverage(Class<?> clazz, Method method, double coverage) {
        String className = ClassUtils.getShortName(clazz);
        String methodName = method.getName();
        System.out.printf("[覆盖率] %s#%s() => %s\n", className, methodName, coverage);
    }

    /**
     * 清理记录
     */
    public static void cleanRecord() {
        threadCoverage.remove();
    }
}