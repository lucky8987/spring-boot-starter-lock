## 分布式锁 - starter
> 基于redis 的 setnx 实现的分布式锁组件

#### 使用场景：

- case1: bean注入获取锁
 
    application.properties(yml) 配置相关的lockName, 例如：
    ```properties
      lock.names = test_lock, demo_lock, temp_lock
    ```
    
    使用方式如下：
    ```java
    import com.example.component;
    import org.springframework.stereotype.Service;  
      
      @Service
      public class CounterService {
            
          /**
           * lockName 来源 application.properties 中配置的 lock.names
           * 推荐使用 @resource 指定 lockName 的方式获取lockBean
           * 当然你也可以使用 @Autowired + @Qualifier("test_lock") 获取lockBean
           * 注意：如果不指定lockName 则默认获取到的 名称为：global_lock 的 lockBean
           */  
          @Resource(name = "test_lock")
          private Lock testLock;
      
          private Integer count = new Integer(100);
          
          public Integer increment() {
              // 占用锁
              testLock.lock();
              while (count > 0) {
                  try {
                      Thread.sleep(10);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  count--;
              }
              // 释放锁
              testLock.unlock();
              return count;
          }
      }
    
    ```
    或者你可以这样使用锁对象，来实现自动释放（推荐使用，如果程序异常退出也能实现自动释放）
    ```java
    import com.example.component;
    import org.springframework.stereotype.Service;  
      
      @Service
      public class CounterService {
            
          /**
           * lockName 来源 application.properties 中配置的 redis-lock.names
           * 推荐使用 @resource 指定 lockName 的方式获取lockBean
           * 当然你也可以使用 @Autowired + @Qualifier("test_lock") 获取lockBean
           * 注意：如果不指定lockName 则默认获取到的 名称为：global_lock 的 lockBean
           */  
          @Resource(name = "test_lock")
          private RedisLock testLock;
      
          private Integer count = new Integer(100);
          
          public Integer increment() {
              // 占用锁, java7+ 特性实现自动unlock
              try (redisLock redisLock = testLock.autoLock()){
                  while (count > 0) {
                      try {
                          Thread.sleep(10);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                      count--;
                  }
              }
              return count;
          }
      }
    ```
- case2: 使用注解加锁, 不需要在application.properties中配置names; 代码如下：

    ```java
      import com.example.annotation.RedisLock;
      import org.springframework.stereotype.Service;
      
      @Service
      public class CounterService {
      
          private Integer count = new Integer(100);
      
          /**
          * aop 实现加锁, 业务方法过粗的情况下不推荐使用
          */
          @RedisLock(name = "test_lock")
          public Integer increment() {
              while (count > 0) {
                  try {
                      Thread.sleep(10);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  count--;
              }
              return count;
          }
      }
    ```
    
> TODO 锁竞争优化、公平锁的实现
    