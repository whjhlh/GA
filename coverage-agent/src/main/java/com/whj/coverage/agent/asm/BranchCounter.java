package com.whj.coverage.agent.asm;

/**
 * @author whj
 * @date 2025-04-11 下午9:50
 */
import java.util.*;

public class BranchCounter {
    // 存储方法签名与总分支数的映射
    private static final Map<String, Integer> totalBranches = new HashMap<>();
    // 存储方法签名与已覆盖分支ID的映射
    private static final Map<String, Set<Integer>> coveredBranches = new HashMap<>();

    static {
        System.out.println("[DEBUG]BranchCounter,ClassLoader: "+BranchCounter.class.getClassLoader());
    }

    public static void hit(String methodSignature, int branchId) {
        coveredBranches.computeIfAbsent(methodSignature, k -> new HashSet<>()).add(branchId);
    }

    public static void setTotalBranches(String methodSignature, int total) {
        totalBranches.put(methodSignature, total);
    }

    public static void reset(String methodSignature) {
        coveredBranches.remove(methodSignature);
    }

    public static double calculateFitness(String methodSignature) {
        int total = totalBranches.getOrDefault(methodSignature, 0);
        if (total == 0) return 0.0;
        int covered = coveredBranches.getOrDefault(methodSignature, Collections.emptySet()).size();
        return (double) covered / total;
    }
}
