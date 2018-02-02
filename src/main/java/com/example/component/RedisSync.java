package com.example.component;

import com.google.common.collect.Maps;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import org.springframework.data.redis.connection.RedisConnectionFactory;


/**
 * 基于redis的锁同步机制
 * 它提供控制临界区的访问和管理正在阻塞等待访问临界区的线程队列的操作
 */
class RedisSync extends AbstractQueuedSynchronizer {

    private String name;

    protected RedisConnectionFactory jedisConnectionFactory;

    private final ConcurrentMap<Thread, LockData> threadData = Maps.newConcurrentMap();

    private static class LockData
    {
        final Thread        owningThread;
        final String        lockName;
        final AtomicInteger lockCount = new AtomicInteger(1);

        private LockData(Thread owningThread, String lockName)
        {
            this.owningThread = owningThread;
            this.lockName = lockName;
        }
    }

    public RedisSync() {

    }


    /**
     * 尝试访问临界区时，调用这个方法。如果线程调用这个方法可以访问临界区，那么这个方法返回true，否则，返回false。
     * @param arg
     * @return
     */
    @Override
    protected boolean tryAcquire(int arg) {
        Thread  currentThread = Thread.currentThread();

        LockData lockData = threadData.get(currentThread);
        if ( lockData != null ) {
            lockData.lockCount.incrementAndGet();
            return true;
        }

        if ( jedisConnectionFactory.getConnection().setNX(name.getBytes(), String.valueOf(arg).getBytes()) ) {
            LockData newLockData = new LockData(currentThread, name);
            threadData.put(currentThread, newLockData);
            return true;
        }
        return false;
    }

    /**
     * 尝试翻译临界区的访问，调用这个方法。如果线程调用这个方法可以释放临界区的访问，那么这个方法返回true，否则，返回false.
     * @param arg
     * @return
     */
    @Override
    protected boolean tryRelease(int arg) {
        Thread      currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        if ( lockData == null )
        {
            throw new IllegalMonitorStateException("You do not own the lock: " + name);
        }

        int newLockCount = lockData.lockCount.decrementAndGet();
        if ( newLockCount < 0 )
        {
            throw new IllegalMonitorStateException("Lock count has gone negative for lock: " + name);
        }
        try
        {
            return jedisConnectionFactory.getConnection().del(name.getBytes()) > 0 ;
        }
        finally
        {
            threadData.remove(currentThread);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        this.jedisConnectionFactory = redisConnectionFactory;
    }
}
