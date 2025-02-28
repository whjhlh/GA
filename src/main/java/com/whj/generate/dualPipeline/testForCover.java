package com.whj.generate.dualPipeline;

/**
 * @author whj
 * @date 2025-03-29 下午7:55
 */
public class testForCover {
    public void test(int a,int b,int c){
        if(a==1){
            if(b==2){
                if(c==3){
                    System.out.println("a==1,b==2,c==3");
                }else {
                    System.out.println("c!=3");
                }
            }else{
                System.out.println("b!=2");
            }
        }
    }
}
