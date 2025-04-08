package com.whj.generate.utill;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.infrastructure.ConditionExtractor;
import com.whj.generate.core.infrastructure.GeneLoader;
import com.whj.generate.whjtest.testForCover;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author whj
 * @date 2025-02-22 下午5:40
 */
@Component
public class GeneticUtil {

    // 类级别添加线程安全的随机数生成器
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();


    private static final GeneLoader genaLoader =new GeneLoader(new ConditionExtractor());

    public static void main(String[] args) {
        Class<testForCover> clazz = testForCover.class;
        long l1=System.currentTimeMillis();
        for(Method method:clazz.getMethods()){
            if(method.getName().equals("test")){
                Population population = buildPopulationModel(clazz, method);
                for(int i=0;i<1000;i++){
                    Chromosome chromosome = population.initChromosome();
                    population.addChromosome(chromosome);
                }
                System.out.println("耗时："+(System.currentTimeMillis() - l1));
                System.out.println(population.getChromosomes().size()+JsonUtil.toJson(population.getChromosomes()));
            }
        }

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
                    population.initChromosome();
                }

            }
        }
        return chromosomeList;
    }

    private static Population buildPopulationModel(Class<?> clazz, Method method) {
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
