package com.example.annotation;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DistributedLock {
    /**
     * 名称
     * @return
     */
    String name() default "global_lock";
}
