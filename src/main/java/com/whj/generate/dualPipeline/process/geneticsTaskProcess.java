package com.whj.generate.dualPipeline.process;

import com.whj.generate.dualPipeline.AbstractDualPipeline;
import com.whj.generate.model.ChromosomeTask;

/**
 * @author whj
 * @date 2025-03-29 下午3:46
 */
public class geneticsTaskProcess extends AbstractDualPipeline<ChromosomeTask> {

    @Override
    protected ChromosomeTask generateTask() {
        return null;
    }

    @Override
    protected boolean processTask(ChromosomeTask task) {
        return false;
    }
}
