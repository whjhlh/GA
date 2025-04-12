package com.whj.coverage.agent;


import com.whj.coverage.agent.asm.BranchCoverageTransformer;

import java.lang.instrument.Instrumentation;

/**
 * @description: 代码覆盖率代理
 * @author whj
 * @date 2025-04-11 下午9:58
 */
public class CoverageAgent {
    public static void premain(String args, Instrumentation inst) {
        System.out.println("[INFO] Coverage Agent is running!");

        BranchCoverageTransformer transformer = new BranchCoverageTransformer();
        inst.addTransformer(transformer);
    }
}
