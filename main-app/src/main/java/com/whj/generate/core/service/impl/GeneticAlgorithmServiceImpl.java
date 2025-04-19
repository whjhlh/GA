package com.whj.generate.core.service.impl;

import com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer;
import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Nature;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer.getCoverageWithPopulation;


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
     * 染色体交叉
     *
     * @param chromosome1 父染色体1
     * @param chromosome2 父染色体2
     */
    public static Chromosome performCrossover(Chromosome chromosome1, Chromosome chromosome2) {

        //属于同一种群才能进行交叉
        if (isSamePopulation(chromosome1, chromosome2)) {
            return null;
        }
        //子染色体初始化
        Chromosome child = new Chromosome(chromosome1.getTargetClass(), chromosome1.getMethod());
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
    public static Chromosome mutation(Chromosome chromosome, double mutationRate) {
        Chromosome mutationChromosome = new Chromosome(chromosome.getTargetClass(), chromosome.getMethod());
        if (null == chromosome) {
            return null;
        }
        Random random = new Random();
        for (int i = 0; i < chromosome.getGenes().length; i++) {
            Parameter parameter = chromosome.getMethod().getParameters()[i];
            //变异
            if (random.nextDouble() > mutationRate) {
                //chromosome.getGenes()[i] = genChromosomeValue(name, parameter.getType());
            }
        }
        return chromosome;
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
     * 两个染色体是否是属于同一个体
     *
     * @param chromosome1 染色体1
     * @param chromosome2 染色体2
     */
    public static boolean isSamePopulation(Chromosome chromosome1, Chromosome chromosome2) {
        if (null == chromosome1 || null == chromosome2) {
            return false;
        }
        return chromosome1.getMethod().equals(chromosome2.getMethod());
    }

    /**
     * 初始化种群
     *
     * @param clazz 方法
     */
    @Override
    public Population initEnvironment(Nature nature, Class<?> clazz, String methodName) {
        Method testMethod = ReflectionUtil.findMethod(clazz, methodName);
        Population population = createPopulationModel(clazz, testMethod);
        processInitPopulation(population);
        getCoverageWithPopulation(nature, population);
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
        while (population.getChromosomeSet().size() < populationSize) {
            Chromosome chromosome = population.initChromosome();
            population.addChromosome(chromosome);
        }
    }

    /**
     * 进化种群
     *
     * @param nature 种群
     * @param count
     * @return
     */
    @Override
    public Population evolvePopulation(Nature nature, Integer count) {
        Population population = nature.getPopulationList().get(count);
        Population newPopulation = createNewPopulation(population);

        // 精英保留（保留历史最优解）
        preserveElites(population, newPopulation);

        // 进化生成新个体
        evolveNewGeneration(nature, population, newPopulation);

        //查看种群覆盖率
        JaCoCoCoverageAnalyzer.getCoverageWithPopulation(nature, newPopulation);
        nature.getPopulationList().add(newPopulation);
        return newPopulation;
    }

    private Population createNewPopulation(Population old) {
        return new Population(
                old.getTargetClass(),
                old.getTargetMethod(),
                old.getGenePool() // 关键点：继承基因库
        );
    }

    /**
     * 精英保留（保留历史最优解）
     * @param src
     * @param dest
     */
    private void preserveElites(Population src, Population dest) {
        src.getChromosomeSet().stream()
                .sorted(Comparator.comparingDouble(Chromosome::getFitness).reversed())
                .limit(GeneticAlgorithmConfig.ELITE_COUNT)
                .forEach(dest::addChromosome);
    }

    private void evolveNewGeneration(Nature nature, Population srcPopulation, Population destPopulation) {
        GenePool genePool = srcPopulation.getGenePool();
        while (destPopulation.getChromosomeSet().size() < srcPopulation.getChromosomeSet().size()) {
            Chromosome child = generateChild(srcPopulation, genePool);
            long fitness = JaCoCoCoverageAnalyzer.calculateFitness(nature, child);
            child.setFitness(fitness);
            destPopulation.addChromosome(child);

        }
    }

    private Chromosome generateChild(Population population, GenePool genePool) {
        // 轮盘赌选择父代
        Chromosome parent1 = selectParentByRoulette(population);
        Chromosome parent2 = selectParentByRoulette(population);

        // 交叉变异
        return performGeneticOperations(parent1, parent2, genePool);
    }

    private Chromosome performGeneticOperations(Chromosome p1, Chromosome p2, GenePool pool) {
        Object[] genes = Arrays.copyOf(p1.getGenes(), p1.getGenes().length);

        // 单点交叉
        if (Math.random() < GeneticAlgorithmConfig.CROSSOVER_RATE) {
            int crossPoint = ThreadLocalRandom.current().nextInt(genes.length);
            for (int i = crossPoint; i < genes.length; i++) {
                genes[i] = p2.getGenes()[i];
            }
        }

        // 基于基因库的变异
        if (Math.random() < getDynamicMutationRate(pool)) {
            mutateGene(genes, pool);
        }

        return new Chromosome(p1.getTargetClass(), p1.getMethod(), genes);
    }

    private void mutateGene(Object[] genes, GenePool pool) {
        int mutatePos = ThreadLocalRandom.current().nextInt(genes.length);
        Object[] availableGenes = pool.getParameterGenes().get(mutatePos);
        genes[mutatePos] = availableGenes[ThreadLocalRandom.current().nextInt(availableGenes.length)];
    }

    private double getDynamicMutationRate(GenePool pool) {
        // 基因类型越多变异率越高
        double diversityFactor = pool.getAverageGeneCount() / 10.0;
        return Math.min(GeneticAlgorithmConfig.MUTATION_RATE * (1 + diversityFactor), 0.35);
    }

    // 增强型轮盘赌选择
    private Chromosome selectParentByRoulette(Population population) {
        double totalFitness = population.getChromosomeSet().stream()
                .mapToDouble(c -> c.getFitness() + 1) // 避免0概率
                .sum();

        double threshold = ThreadLocalRandom.current().nextDouble() * totalFitness;
        double accumulator = 0;

        for (Chromosome c : population.getChromosomeSet()) {
            accumulator += (c.getFitness() + 1);
            if (accumulator >= threshold) {
                return c;
            }
        }
        return population.getChromosomeSet().iterator().next();
    }
}
