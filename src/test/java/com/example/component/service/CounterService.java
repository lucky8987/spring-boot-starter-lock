package com.example.component.service;

import com.example.annotation.DistributedLock;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private Integer count = new Integer(100);

    @DistributedLock(name = "test_lock")
    public Integer increment() {
        while (count > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count--;
        }
        return count;
    }
}
