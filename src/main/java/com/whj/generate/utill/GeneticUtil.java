package com.whj.generate.utill;

import com.whj.generate.model.Chromosome;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author whj
 * @date 2025-02-22 下午5:40
 */
public class GeneticUtil {
    /**
     * 初始化种群
     *
     * @param clazz 方法
     */
    public static List<Chromosome> initPopulation(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        List<Chromosome> chromosomeList = new ArrayList<>();
        //每个方法意味着一个个体
        for (Method method : methods) {
            //final方法不可动态调用
            if (!ReflectionUtil.isFinalMethod(method)) {
                chromosomeList.add(randomChromosome(method));
            }
        }
        return chromosomeList;
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
                chromosome.getGenes()[i] = randomValue(parameter.getType());
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

    /**
     * 随机生成某个种群的染色体
     *
     * @param method 方法(种群)
     */
    public static Chromosome randomChromosome(Method method) {
        Chromosome chromosome = new Chromosome(method);
        //生成染色体的基因
        for (int i = 0; i < chromosome.getGenes().length; i++) {
            chromosome.getGenes()[i] = randomValue(chromosome.getMethod().getParameters()[i].getType());
        }
        return chromosome;
    }

    /**
     * 根据class类型随机生成值<br/>
     * 目前只支持int,long,boolean,enum,Integer,Boolean,Long
     */
    public static Object randomValue(Class<?> type) {
        //todo 范围需要可控
        Random random = new Random();
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return random.nextInt(100);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return random.nextLong();
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return random.nextBoolean();
        } else if (type.isEnum()) {
            return random.nextInt(type.getEnumConstants().length);
        } else {
            return null;
        }
    }
}
