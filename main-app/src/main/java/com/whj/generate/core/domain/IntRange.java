package com.whj.generate.core.domain;

// 行号范围记录
public record IntRange(int start, int end) {
    boolean contains(int line) {
        return line >= start && line <= end;
    }
}
