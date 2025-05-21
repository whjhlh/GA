package com.whj.generate.whjtest;

import com.whj.generate.core.bizenum.ComparisonOperatorEnum;
import com.whj.generate.core.bizenum.LogicalOperatorEnum;

/**
 * @author whj
 * @date 2025-03-29 下午7:55
 */
public class TestForCover {
    public static void test(int a, int b, int c, LogicalOperatorEnum op, ComparisonOperatorEnum enmu) {
        if (enmu.getCode().equals("equals")) {
            System.out.print("");
        }
        if(op.getCode().equals("and")){
            if (b == 2) {
                if (c == 3 && enmu == ComparisonOperatorEnum.LESS) {
                    System.out.print("");
                } else {
                    System.out.print("");
                }
            } else if (b > 3) {
                System.out.print("");
            }
        }
        if (a == 1 || op == LogicalOperatorEnum.OR) {
            if (b == 2) {
                if (c == 3 && enmu == ComparisonOperatorEnum.LESS) {
                    System.out.print("");
                } else {
                    System.out.print("");
                }
            } else if (b > 3) {
                System.out.print("");
            }
        } else if (a < 1) {
            System.out.print("");
        } else if (a > 1) {
            System.out.print("");
        }
        if (a == 8) {
            if (b == 12) {
                if (c == 6) {
                    System.out.print("");
                } else if (op.getCode().equals("minus")){
                    System.out.print("");
                }
            }
        }
        if (a == 9) {
            if (b == 13) {
                if (c == 12) {
                } else if (op.getCode().equals("minus")){
                    throw new RuntimeException("异常信息mock");
                }
            }
        }
    }
    public void test2(int a,int b,int c){
        if(a==1){
            if(b==2){
                if(c==3){
                    System.out.print("");
                }
                a=3;
            }
            a=2;
            c=4;
        }
    }
}
