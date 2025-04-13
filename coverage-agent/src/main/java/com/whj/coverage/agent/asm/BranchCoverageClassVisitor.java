package com.whj.coverage.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
/**
 * @author whj
 * @date 2025-04-11 下午9:51
 */
public class BranchCoverageClassVisitor extends ClassVisitor {
    private String className;

    public BranchCoverageClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(name.contains("lambda$")){
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new BranchCoverageMethodVisitor(className, name, desc, mv);
    }
}