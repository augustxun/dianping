package com.dp.utils;

public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true代表获取成功 ,false 代表失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁操作
     */
    void unlock();
}
