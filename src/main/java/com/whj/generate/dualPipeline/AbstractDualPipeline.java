package com.whj.generate.dualPipeline;

import com.google.common.util.concurrent.Striped;
import com.whj.generate.utill.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

/**
 * 抽象DualPipe任务处理器
 *
 * @author whj
 * @date 2025-03-29 下午2:14
 */
public abstract class AbstractDualPipeline<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDualPipeline.class);
    /**
     * 任务队列容量
     */
    protected final BlockingDeque<T> taskQueue = new LinkedBlockingDeque<>();
    /**
     * 死信对列
     */
    protected final BlockingDeque<T> deadLetterQueue = new LinkedBlockingDeque<>();
    /**
     * 已处理任务数
     */
    protected final AtomicLong processedCount = new AtomicLong(0);
    /**
     * 失败任务数
     */
    protected final AtomicLong failedCount = new AtomicLong(0);
    /**
     * 并发池
     */
    protected final ForkJoinPool forkJoinPool = new ForkJoinPool();
    protected final Phaser piplinePhaser = new Phaser(1);
    protected final Striped<Lock> stateLock = Striped.lock(4);
    /**
     * 成功任务集合
     */
    private final List<T> successList = new CopyOnWriteArrayList<>();
    /**
     * 生产者线程
     */
    protected ExecutorService producerExecutor;
    /**
     * 消费者线程
     */
    protected ExecutorService consumerExecutor;
    /**
     * 是否正在运行
     */
    protected volatile boolean isRunning = false;

    protected abstract T generateTask();

    protected abstract boolean processTask(T task);

    /**
     * 启动双流水线
     *
     * @return
     */
    public synchronized void start() throws InterruptedException {
        if (isRunning) return;
        isRunning = true;

        initExecutor();
        startProducers();
        startConsumers();
    }
//
//    private synchronized void onShutDown() {
//        isRunning = false;
//        try {
//            consumerExecutor.shutdown();
//            producerExecutor.shutdown();
//            forkJoinPool.shutdown();
//            consumerExecutor.awaitTermination(10, TimeUnit.SECONDS);
//            producerExecutor.awaitTermination(10, TimeUnit.SECONDS);
//            forkJoinPool.awaitTermination(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            producerExecutor.shutdownNow();
//            consumerExecutor.shutdownNow();
//            forkJoinPool.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }

    private void startConsumers() {
        //实时管道处理
        startRealTimePipeline();
        //批量处理管道（高吞吐）
        startBatchTimePipeline();
    }

    private void startBatchTimePipeline() {
        System.out.println("批量处理管道");
        consumerExecutor.submit(() -> {
            List<T> batch = new ArrayList<>(dynamicBatchSize());
            while (isRunning) {
                try {
                    T task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        batch.add(task);
                        if (batch.size() >= dynamicBatchSize()) {
                            processBatchWithPipeline(batch);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * 批量处理管道
     *
     * @param batch
     */
    private void processBatchWithPipeline(List<T> batch) {
        piplinePhaser.register();
        try {
            batch.parallelStream().forEach(batchTask -> {
                processSingleTasksWithLock(batchTask);
            });
        } finally {
            piplinePhaser.arriveAndDeregister();
        }
        batch.clear();
    }

    /**
     * 任务执行重试
     *
     * @param task
     * @return
     */
    private void processTaskWithRetry(T task) {
        int attempts = 0;
        boolean res=false;
        //结果成功跳出循环
        while (attempts <= maxRetries() && !res) {
            try {
                res = processTask(task);
                if (res) {
                    processedCount.incrementAndGet();
                    synchronized (successList) {
                        successList.add(task); // 同步添加操作
                    }
                } else {
                    failedCount.incrementAndGet();
                }
            } catch (Exception e) {
                attempts++;
                logger.error(String.format("任务处理失败,attempts=%s,failed for task=%s", attempts, task), e);
            }
        }
        try {
            deadLetterQueue.put(task);
        } catch (InterruptedException e) {
            logger.error("BlockingDequeUtil putWithOutException error", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
    }

    private void startRealTimePipeline() {
        System.out.println("实时管道处理");
        consumerExecutor.submit(() -> {
            while (isRunning && !taskQueue.isEmpty()) {
                T task= taskQueue.poll();
                forkJoinPool.submit(() -> {
                    processSingleTasksWithLock(task);
                });

            }
        });
        System.out.println(JsonUtil.toJson(taskQueue));
        System.out.println("实时管道处理结束");
    }

    /**
     * 单任务处理（加锁）
     *
     * @param task
     */
    private void processSingleTasksWithLock(T task) {
        Lock lock = lockTask(task);
        try {
            processTaskWithRetry(task);
        } finally {
            lock.unlock();
        }
    }

    private Lock lockTask(T task) {
        Lock lock = stateLock.get(task.hashCode());
        lock.lock();
        return lock;
    }

    private void startProducers() {
            producerExecutor.submit(() -> {
                while (isRunning && taskQueue.isEmpty()) {
                    try {
                        T task = generateTask();
                        //放入任务队列中
                        taskQueue.put(task);
                    } catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        System.out.println("生成任务失败");
                        logger.error("生成任务失败", e);
                    }
                }
            });
        System.out.println(JsonUtil.toJson(taskQueue));
    }

    private void initExecutor() {
        producerExecutor = new ForkJoinPool(
                producerThreads(),
                new NamePrefixThreadFactory("producer"),
                null,
                true
        );
        consumerExecutor = Executors.newWorkStealingPool(consumerThreads());
    }

    private int dynamicBatchSize() {
        int base = batchSize();
        double loadFactor = taskQueue.size() / (double) queueCapacity();
        return Math.min((int) (base * (1 + Math.sqrt(loadFactor))), queueCapacity() / 10);
    }

    protected int queueCapacity() {
        return 10000;
    }

    protected int maxRetries() {
        return 3;
    }

    /**
     * 批次数
     */
    protected int batchSize() {
        return 50;
    }

    /**
     * 生产者线程数
     */
    protected int producerThreads() {
        return 1;
    }

    /**
     * 消费者线程数
     */
    protected int consumerThreads() {
        //系统处理其核心数
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    /**
     * 获取成功列表
     *
     * @return
     */
    public List<T> getSuccessList() {
        return Collections.unmodifiableList(successList);
    }
}
