package com.example.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lock")
public class LockPropertes {

    public static final String default_name = "global_lock";
    public static final String zk_root_path = "/locks";

    private String zkHost = "localhost";

    private LockType type;

    private String redisHost = "redis://127.0.0.1";

    private String redisPassword;

    private int redisPort = 6379;

    private int zkPort = 2181;

    private int zkConnectionTimeOut = 2000;


    private String[] names;

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public int getZkPort() {
        return zkPort;
    }

    public void setZkPort(int zkPort) {
        this.zkPort = zkPort;
    }

    public int getZkConnectionTimeOut() {
        return zkConnectionTimeOut;
    }

    public void setZkConnectionTimeOut(int zkConnectionTimeOut) {
        this.zkConnectionTimeOut = zkConnectionTimeOut;
    }

    public String getZkHost() {
        return zkHost;
    }

    public LockType getType() {
        return type;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public void setZkHost(String zkHost) {
        this.zkHost = zkHost;
    }

    public String getZkConnectionStr() {
        return zkHost.concat(":").concat(String.valueOf(zkPort));
    }

    public String getRedisConnectionStr() {
        return redisHost.concat(":").concat(String.valueOf(redisPort));
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }
}
