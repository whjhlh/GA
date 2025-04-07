package com.whj.generate.dualPipeline;

import com.whj.generate.model.bizenum.ComparisonOperatorEnum;
import com.whj.generate.model.bizenum.LogicalOperatorEnum;

/**
 * @author whj
 * @date 2025-03-29 下午7:55
 */
public class testForCover {
    public void test(int a, int b, int c, LogicalOperatorEnum op, ComparisonOperatorEnum enmu) {

        if (enmu.getCode().equals("equals")) {
            System.out.println("a==1,b==2,c==3");
        }
        if(op.getCode().equals("and")){
            if (b == 2) {
                if (c == 3 && enmu == ComparisonOperatorEnum.LESS) {
                    System.out.println("a==1,b==2,c==3");
                } else {
                    System.out.println("c!=3");
                }
            } else if (b > 3) {
                System.out.println("b!=2");
            }
        }
        if (a == 1 || op == LogicalOperatorEnum.OR) {
            if (b == 2) {
                if (c == 3 && enmu == ComparisonOperatorEnum.LESS) {
                    System.out.println("a==1,b==2,c==3");
                } else {
                    System.out.println("c!=3");
                }
            } else if (b > 3) {
                System.out.println("b!=2");
            }
        } else if (a < 1) {
            System.out.println("a!=1");
        } else if (a > 1) {
            System.out.println("a>1");
        }
    }

}
