package com.example.component;

import com.example.config.LockConfigure;
import com.example.component.service.CounterService;
import com.sun.istack.internal.NotNull;
import java.util.concurrent.locks.Lock;
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LockConfigure.class, properties = "classpath:application.properties")
@Import(CounterService.class)
public class RedisLockTest {

    @Resource(name = "test_lock")
    private Lock redisLock;

    @Resource(name = "demo_lock")
    private Lock demoLock;

    @Autowired
    private CounterService service;

    @Test
    public void lockTest() {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                redisLock.lock();
                try{
                    System.out.println(Thread.currentThread().getName() + "获得lock");
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                redisLock.unlock();
            }).start();

        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void countTest() {
        ThreadGroup threadGroup = new ThreadGroup("counterTask");
        for (int i = 0; i < 10; i ++) {
            new Thread(threadGroup,() -> {
                int count = service.increment();
                System.err.println(Thread.currentThread().getName() + ": count="+count);
            }).start();
        }

        while (threadGroup.activeCount() > 1);
    }
}
