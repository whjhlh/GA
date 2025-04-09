package com.whj.generate.utill;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.infrastructure.ConditionExtractor;
import com.whj.generate.core.infrastructure.GeneLoader;
import com.whj.generate.whjtest.testForCover;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * @author whj
 * @date 2025-02-22 下午5:40
 */
@Component
public class GeneticUtil {


    private static final GeneLoader genaLoader = new GeneLoader(new ConditionExtractor());
    // 缓存方法元数据
    private static final Map<Class<?>, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2);

    public static void main(String[] args) {
        Class<testForCover> clazz = testForCover.class;
        Method testMethod = ReflectionUtil.findMethod(clazz, "test");
        long start = System.nanoTime();
        Population population = buildPopulationModel(clazz, testMethod);
        initPopulation(population);
        long initTime = System.nanoTime() - start;
        reportedInFile(initTime, population);


    }

    /**
     * 初始化种群
     *
     * @param population
     */
    private static void initPopulation(Population population) {
        if (null == population) {
            return;
        }
        int populationSize = getPopulationSize(population);
        try {
            ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2);
            pool.submit(() -> IntStream.range(0, populationSize)
                    .parallel()
                    .forEach(i -> {
                        Chromosome chromosome = population.initChromosome();
                        population.addChromosome(chromosome);
                    })
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成报告
     *
     * @param initTime
     * @param population
     */
    private static void reportedInFile(long initTime, Population population) {
        if (null == population) {
            return;
        }
        GenePool genePool = population.getGenePool();
        List<Object[]> list = population.getChromosomes().stream().map(Chromosome::getGenes).toList();
        StringBuilder report = new StringBuilder(1024);
        //日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        report.append("=== 种群初始化报告 ===\n")
                .append(String.format("初始化时间：%s\n", dateFormat.format(date)))
                .append(String.format("耗时：%.3f ms\n", initTime / 1e6))
                .append("基因库概况：\n")
                .append(JsonUtil.toJson(genePool.getParameterGenes()))
                .append("生成染色体数: \n")
                .append(list.size())
                .append("\n")
                .append("=== 种群初始化结果 ===\n");
        for (Object[] genes : list) {
            report.append(JsonUtil.toJson(genes)).append("\n");
        }

// 写入文件代替控制台输出
        try {
            Files.writeString(Paths.get("population_report.txt"),
                    report.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化种群大小 = (avg_genes)^(k/2)
     *
     * @param population
     * @return
     */
    private static int getPopulationSize(Population population) {
        if (null == population) {
            return 0;
        }
        return (int) Math.pow(
                population.getGenePool().getAverageGeneCount(),
                0.5 * population.getGenePool().getParameterCount()
        );
    }

    /**
     * 初始化种群
     *
     * @param clazz 方法
     */
    public static List<Chromosome> initEnvironment(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        List<Chromosome> chromosomeList = new ArrayList<>();
        //每个方法意味着一个个体
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                //final方法不可动态调用
                if (!ReflectionUtil.isFinalMethod(method)) {
                    Population population = buildPopulationModel(clazz, method);
                    double maxGeneCount = (int) population.getGenePool().getAverageGeneCount();
                    for (int i = 0; i < maxGeneCount; i++) {
                        population.initChromosome();
                    }
                }

            }
        }
        return chromosomeList;
    }

    private static Population buildPopulationModel(Class<?> clazz, Method method) {
        if (null == clazz || null == method) {
            return null;
        }
        GenePool genePool = genaLoader.loadGenePool(clazz, method);
        return new Population(clazz, method, genePool);
    }

    /**
     * 染色体交叉
     *
     * @param chromosome1 父染色体1
     * @param chromosome2 父染色体2
     */
    public static Chromosome crossOver(Chromosome chromosome1, Chromosome chromosome2) {

        //属于同一种群才能进行交叉
        if (isSamePopulation(chromosome1, chromosome2)) {
            return null;
        }
        //子染色体初始化
        Chromosome child = new Chromosome(chromosome1.getMethod());
        for (int i = 0; i < chromosome1.getGenes().length; i++) {
            int point = new Random().nextInt(chromosome1.getGenes().length);
            if (i < point) {
                child.getGenes()[i] = chromosome1.getGenes()[i];
            } else {
                child.getGenes()[i] = chromosome2.getGenes()[i];
            }
        }
        return child;
    }

    /**
     * 染色体变异
     *
     * @param chromosome   染色体
     * @param mutationRate 变异概率
     */
    public static void mutation(Chromosome chromosome, double mutationRate) {
        if (null == chromosome) {
            return;
        }
        Random random = new Random();
        for (int i = 0; i < chromosome.getGenes().length; i++) {
            Parameter parameter = chromosome.getMethod().getParameters()[i];
            //变异
            if (random.nextDouble() > mutationRate) {
                //chromosome.getGenes()[i] = genChromosomeValue(name, parameter.getType());
            }
        }
    }

    /**
     * 两个染色体是否是属于同一个体
     *
     * @param chromosome1 染色体1
     * @param chromosome2 染色体2
     */
    public static boolean isSamePopulation(Chromosome chromosome1, Chromosome chromosome2) {
        if (null == chromosome1) {
            return false;
        }
        return chromosome1.getMethod().equals(chromosome2.getMethod());
    }

}
