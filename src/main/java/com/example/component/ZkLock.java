package com.example.component;

import com.example.properties.LockPropertes;
import com.netflix.curator.framework.imps.CuratorFrameworkImpl;
import com.netflix.curator.framework.recipes.locks.InterProcessMutex;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * zk 分布式锁实现
 *
 */
public class ZkLock implements Lock, AutoCloseable {

    private InterProcessMutex sync;

    public ZkLock(CuratorFrameworkImpl zkClient, String path) {
        this.sync = new InterProcessMutex(zkClient, LockPropertes.zk_root_path.concat("/").concat(path));
    }

    public ZkLock() {

    }

    public ZkLock autoLock() {
        lock();
        return this;
    }

    @Override
    public void lock() {
        try {
            sync.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lockInterruptibly() throws InterruptedException {
        try {
            sync.makeRevocable(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean tryLock() {
        try {
            return sync.acquire(-1, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            return sync.acquire(time, unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unlock() {
        try {
            sync.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }


    @Override
    public void close() throws Exception {
       unlock();
    }
}
