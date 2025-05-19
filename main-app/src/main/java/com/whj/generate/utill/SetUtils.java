package com.whj.generate.utill;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author whj
 * @date 2025-05-19 下午2:55
 */
public class SetUtils {
    /**
     * 交集
     */
    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        return a.stream().filter(b::contains).collect(Collectors.toSet());
    }
    /**
     * 并集
     */
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> union = new HashSet<>(a);
        union.addAll(b);
        return union;
    }
    /**
     * 左差集
     */
    public static <T> Set<T> leftDifference(Set<T> a, Set<T> b) {
        Set<T> leftDifference = new HashSet<>(a);
        leftDifference.removeAll(b);
        return leftDifference;
    }
    /**
     * 右差集
     */
    public static <T> Set<T> rightDifference(Set<T> a, Set<T> b) {
        Set<T> rightDifference = new HashSet<>(b);
        rightDifference.removeAll(a);
        return rightDifference;
    }

}
