package com.github.pingaz.idgen.seeds.redis;

import redis.clients.jedis.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * @author ping
 */
public class JedisAdapter implements RedisAdapter, Closeable {

    public static class RedisConfig{
        private String hostName;
        private String password;
        private int port;
        private Set<HostAndPort> clusterNodes;

        public RedisConfig(String hostName, int port, String password) {
            this.hostName = hostName;
            this.password = password;
            this.port = port;
        }

        public RedisConfig(Set<HostAndPort> clusterNodes) {
            this.clusterNodes = clusterNodes;
        }
    }

    private JedisCommands jedis;

    public JedisAdapter(String host, int port, String password){
        this(new RedisConfig(host, port, password));
    }

    public JedisAdapter(Set<HostAndPort> clusterNodes){
        this(new RedisConfig(clusterNodes));
    }

    public JedisAdapter(RedisConfig config){
        if(config.clusterNodes != null) {
            this.jedis = new JedisCluster(config.clusterNodes);
        } else if(config.hostName != null && config.port != 0){  //单机模式
            JedisShardInfo shardInfo = new JedisShardInfo(config.hostName, config.port);
            if (config.password != null && !"".equals(config.password)) {
                shardInfo.setPassword(config.password);
            }
            jedis = new Jedis(shardInfo);
        } else {
            throw new RuntimeException("Redis config error , clusterNodes and hostname are all null .");
        }
    }

    @Override
    public String get(String key) {
        return jedis.get(key);
    }

    @Override
    public boolean set(String key, String value, int seconds) {
        String reply = jedis.set(key, value, "NX", "EX", seconds);
        return "OK".equalsIgnoreCase(reply);
    }

    @Override
    public boolean expire(String key, int seconds) {
        return jedis.expire(key, seconds) > 0;
    }

    @Override
    public boolean del(String key) {
        return jedis.del(key) > 0;
    }

    @Override
    public void close() throws IOException {
        if(jedis instanceof Closeable){
            ((Closeable)jedis).close();
        }
    }
}
