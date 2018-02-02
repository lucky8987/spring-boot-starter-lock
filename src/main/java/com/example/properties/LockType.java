package com.example.properties;

import com.example.component.RedisLock;
import com.example.component.ZkLock;


/**
 * 分布式锁的实现-类型
 */
public enum LockType {

    REDIS(RedisLock.class),

    ZK(ZkLock.class);

    public Class implClazz;

    LockType(Class implClazz) {
        this.implClazz = implClazz;
    }

}
