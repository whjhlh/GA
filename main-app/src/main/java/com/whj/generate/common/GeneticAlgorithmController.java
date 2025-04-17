package com.whj.generate.common;


import com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.service.GeneticAlgorithmService;
import com.whj.generate.whjtest.testForCover;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author whj
 * @date 2025-04-10 上午2:46
 */
@Controller
public class GeneticAlgorithmController {
    @Autowired
    private GeneticAlgorithmService geneticAlgorithmService;
    public void test() {
        Class<testForCover> clazz = testForCover.class;
        Population population = geneticAlgorithmService.initEnvironment(clazz, "test");
        Set<Chromosome> chromosomes = population.getChromosomes();
        fillFitness(chromosomes);
    }

    private void fillFitness(Set<Chromosome> chromosomes) {
        IAgent agent = RT.getAgent(); // 获取JaCoCo Agent实例
        try {
            for (Chromosome chromosome : chromosomes) {
                Method method = chromosome.getMethod();
                Object[] params = chromosome.getGenes();
                chromosome.setFitness(JaCoCoCoverageAnalyzer.getFitness(agent, method, params));
                System.out.println(chromosome.getFitness());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
