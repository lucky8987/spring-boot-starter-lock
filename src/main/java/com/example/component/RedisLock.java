package com.example.component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import javax.annotation.PreDestroy;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * redis 分布式锁实现
 *
 */
public class RedisLock implements Lock {

    private RedissonClient redisson;

    private String lockName;

    private RLock rLock;

    public RedisLock(Redisson redisson, String name) {
        this.rLock = redisson.getLock(name);
    }

    public RedisLock() {

    }

    @Override
    public void lock() {
        rLock.lock();
    }

    public void lockInterruptibly() throws InterruptedException {
        rLock.lockInterruptibly();
    }

    public boolean tryLock() {
        return rLock.tryLock();
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return rLock.tryLock(time, unit);
    }

    public void unlock() {
        rLock.unlock();
    }

    public Condition newCondition() {
        return rLock.newCondition();
    }

}
