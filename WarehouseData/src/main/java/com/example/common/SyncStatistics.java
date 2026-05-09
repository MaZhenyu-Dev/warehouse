package com.example.common;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程安全的同步统计类
 */
@Data
public class SyncStatistics {
    private final AtomicInteger insertCount = new AtomicInteger(0);
    private final AtomicInteger deleteCount = new AtomicInteger(0);
    private final AtomicInteger updateCount = new AtomicInteger(0);
    private final AtomicInteger skipCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger processedPages = new AtomicInteger(0);

    public void addInsert(int count) {
        insertCount.addAndGet(count);
    }

    public void addDelete(int count) {
        deleteCount.addAndGet(count);
    }

    public void addUpdate(int count) {
        updateCount.addAndGet(count);
    }

    public void addSkip(int count) {
        skipCount.addAndGet(count);
    }

    public void addError(int count) {
        errorCount.addAndGet(count);
    }

    public void incrementPage() {
        processedPages.incrementAndGet();
    }


    @Override
    public String toString() {
        return String.format("插入: %d | 删除: %d | 更新: %d | 跳过: %d | 错误: %d | 完成页数: %d",
                insertCount.get(),deleteCount.get(), updateCount.get(), skipCount.get(),
                errorCount.get(), processedPages.get());
    }
}