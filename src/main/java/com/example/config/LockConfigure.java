package com.example.config;

import com.example.component.RedisLock;
import com.example.component.ZkLock;
import com.example.properties.LockPropertes;
import com.example.properties.LockType;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RedisLock.class)
@EnableAutoConfiguration
@EnableConfigurationProperties(LockPropertes.class)
public class LockConfigure{

    private static final String lock_config_source = "lock_config_source";

    @Autowired
    private LockPropertes lockPropertes;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private ApplicationContext context;


    @Bean(lock_config_source)
    @ConditionalOnProperty(name = "lock.type", havingValue = "zk")
    public CuratorFramework zooKeeperClient() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(lockPropertes.getZkConnectionStr(), new ExponentialBackoffRetry(1000, 3));
        client.start();
        return client;
    }

  /*  @Bean(lock_config_source)
    @ConditionalOnProperty(name = "lock.type", havingValue = "redis")
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(lockPropertes.getRedisHost());
        config.setPort(lockPropertes.getRedisPort());
        config.setPassword(RedisPassword.of(lockPropertes.getRedisPassword()));
        return new JedisConnectionFactory(config, JedisClientConfiguration.defaultConfiguration());
    }*/

    @Bean(lock_config_source)
    @ConditionalOnProperty(name = "lock.type", havingValue = "redis")
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress(lockPropertes.getRedisConnectionStr());
        return Redisson.create(config);
    }


    @Bean(LockPropertes.default_name)
    public Lock initLock() {
        LockType lockType = lockPropertes.getType();
        Object configBean = context.getBean(lock_config_source);
        Optional.ofNullable(configBean).ifPresent(bean -> Arrays.stream(lockPropertes.getNames()).forEach(e -> {
            // 动态注册bean
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(lockType.implClazz);
            // 设置构造函数参数
            ConstructorArgumentValues ctorValues = new ConstructorArgumentValues();
            ctorValues.addGenericArgumentValue(new ValueHolder(bean, bean.getClass().getName()));
            ctorValues.addGenericArgumentValue(new ValueHolder(e, e.getClass().getName()));
            beanDefinition.setConstructorArgumentValues(ctorValues);
            beanFactory.registerBeanDefinition(e, beanDefinition);
        }));

        Object lock = null;
        try {
            lock = lockType.implClazz.getConstructor(configBean.getClass(), String.class).newInstance(configBean, LockPropertes.default_name);
        } catch (Exception e){
            try {
                lock = lockType.implClazz.newInstance();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        System.out.println((char)27 + "["+ AnsiColor.GREEN+"Successfully create a distributed lock by "+ lockPropertes.getType() +" with a name: "+ Arrays.toString(lockPropertes.getNames()) + (char)27 + "[0m");
        return lock == null ? null : (Lock) lock;
    }

}
