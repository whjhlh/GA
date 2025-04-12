package com.whj.generate.core.service.impl;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.infrastructure.PoolLoader;
import com.whj.generate.core.service.GeneticAlgorithmService;
import com.whj.generate.utill.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static com.whj.generate.utill.FileUtil.reportedInFile;

/**
 * @author whj
 * @date 2025-02-22 下午5:40
 */
@Service
public class GeneticAlgorithmServiceImpl implements GeneticAlgorithmService {
    private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithmServiceImpl.class);

    private final PoolLoader<GenePool> poolLoader;
    private final ForkJoinPool geneticThreadPool;

    @Autowired
    public GeneticAlgorithmServiceImpl(
            PoolLoader<GenePool> poolLoader,
            @Qualifier("geneticForkJoinPool") ForkJoinPool geneticThreadPool
    ) {
        this.poolLoader = poolLoader;
        this.geneticThreadPool = geneticThreadPool;
    }

    /**
     * 初始化种群
     *
     * @param clazz 方法
     */
    @Override
    public Population initEnvironment(Class<?> clazz, String methodName) {
        Method testMethod = ReflectionUtil.findMethod(clazz, methodName);
        long start = System.nanoTime();
        Population population = createPopulationModel(clazz, testMethod);
        processInitPopulation(population);
        long initTime = System.nanoTime() - start;
        reportedInFile(initTime, population);
        return population;
    }

    /**
     * 处理初始化种群
     *
     * @param population
     */
    private void processInitPopulation(Population population) {
        if (null == population) {
            return;
        }
        int populationSize = calculatePopulationSize(population);
        try {
            geneticThreadPool.submit(() -> IntStream.range(0, populationSize)
                    .parallel()
                    .forEach(i -> {
                        Chromosome chromosome = population.initChromosome();
                        population.addChromosome(chromosome);
                    })
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        //兜底补充，解决并行带来的问题
        while (population.getChromosomes().size()<populationSize){
            Chromosome chromosome = population.initChromosome();
            population.addChromosome(chromosome);
        }
    }


    /**
     * 初始化种群大小 = (avg_genes)^(k/2)
     *
     * @param population
     * @return
     */
    private int calculatePopulationSize(Population population) {
        if (null == population) {
            return 0;
        }
        GenePool genePool = population.getGenePool();
        return (int) Math.pow(genePool.getAverageGeneCount(), 0.5 * genePool.getParameterCount());
    }

    private Population createPopulationModel(Class<?> clazz, Method method) {
        if (null == clazz || null == method) {
            return null;
        }
        GenePool genePool = poolLoader.initializePool(clazz, method);
        return new Population(clazz, method, genePool);
    }

    /**
     * 染色体交叉
     *
     * @param chromosome1 父染色体1
     * @param chromosome2 父染色体2
     */
    public Chromosome performCrossover(Chromosome chromosome1, Chromosome chromosome2) {

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
