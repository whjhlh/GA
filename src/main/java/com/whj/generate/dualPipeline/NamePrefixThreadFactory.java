package com.whj.generate.dualPipeline;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * @author whj
 * @date 2025-03-29 下午2:33
 */
public class NamePrefixThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    /**
     * 线程名前缀
     */
    private final String namePrefix;

    public NamePrefixThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName(namePrefix + "-" + worker.getPoolIndex());
        System.out.println(worker.getName());
        return worker;
    }
}
