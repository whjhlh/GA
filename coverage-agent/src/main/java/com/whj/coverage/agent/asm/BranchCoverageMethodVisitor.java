package com.whj.coverage.agent.asm;

/**
 * @author whj
 * @date 2025-04-11 下午9:52
 */

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class BranchCoverageMethodVisitor extends MethodVisitor {
    public static final String OWNER = BranchCounter.class.getName().replace(".", "/");
    private final String methodSignature;
    private int branchCounter = 0;
    public BranchCoverageMethodVisitor(String className, String methodName, String methodDesc, MethodVisitor mv) {
        super(Opcodes.ASM9, mv);
        this.methodSignature = className + "." + methodName + methodDesc;
    }
    static {
        System.out.println("BranchCoverageMethodVisitor,Owner: "+ OWNER);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        // 插入统计代码：BranchCounter.hit(methodSignature, branchCounter)
        mv.visitLdcInsn(methodSignature);
        mv.visitLdcInsn(branchCounter);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, OWNER, "hit", "(Ljava/lang/String;I)V", false);

        super.visitJumpInsn(opcode, label);
        branchCounter++;
    }

    @Override
    public void visitEnd() {
        // 设置该方法的总分支数
        if (branchCounter > 0) {
            mv.visitLdcInsn(methodSignature);
            mv.visitLdcInsn(branchCounter);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, OWNER, "setTotalBranches", "(Ljava/lang/String;I)V", false);
        }
        super.visitEnd();
    }
}
