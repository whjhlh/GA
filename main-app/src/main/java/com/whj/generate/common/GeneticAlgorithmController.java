package com.whj.generate.common;

import com.whj.generate.core.service.GeneticAlgorithmService;
import com.whj.generate.whjtest.testForCover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author whj
 * @date 2025-04-10 上午2:46
 */
@Controller
public class GeneticAlgorithmController {
    @Autowired
    private GeneticAlgorithmService geneticAlgorithmService;
    public void test() {
        geneticAlgorithmService.initEnvironment(testForCover.class, "test");
    }
}
