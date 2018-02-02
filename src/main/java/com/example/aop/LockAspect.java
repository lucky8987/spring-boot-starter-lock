package com.example.aop;

import com.example.annotation.DistributedLock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Scope("prototype")
public class LockAspect {

    @Autowired
    private Map<String, Lock>  lockMap;

    /**
     * 开始方法之前创建lock-bean
     * @param
     */
    @Before(value = "@annotation(lock)")
    public void beforeInitBean(DistributedLock lock) {
        // 获取锁
        Lock currentLock = lockMap.get(lock.name());
        Optional.ofNullable(currentLock).ifPresent(l -> {
            // 占用锁
            l.lock();
            System.err.println(Thread.currentThread().getName() + "：占用锁-"+lock.name());
        });
    }

    /**
     * 自动释放锁
     * @param
     */
    @After(value = "@annotation(lock)")
    public void afterReleaseLock(DistributedLock lock) {
        // 获取锁
        Lock currentLock = lockMap.get(lock.name());
        Optional.ofNullable(currentLock).ifPresent(l -> {
            l.unlock();
            System.err.println(Thread.currentThread().getName() + "：释放锁-"+lock.name());
        });
    }
}
