package com.whj.generate.algorithm.strategy;

import com.whj.generate.model.MethodStructure;
import com.whj.generate.model.TestCase;

/**
 * 策略接口
 *
 * @author whj
 * @date 2025-01-01 下午7:09
 */
public interface Strategy {
    TestCase generateTestCase(MethodStructure method);
}

