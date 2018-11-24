package com.github.pingaz.idgen.seeds.redis;

/**
 * @author ping
 */
public interface RedisAdapter {

    String get(String key);

    boolean set(String key, String value, int seconds);

    boolean expire(String key, int seconds);

    boolean del(String key);
}
