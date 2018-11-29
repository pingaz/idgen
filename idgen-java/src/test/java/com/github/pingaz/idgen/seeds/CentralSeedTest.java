package com.github.pingaz.idgen.seeds;

import com.github.pingaz.idgen.seeds.redis.JedisAdapter;
import com.github.pingaz.idgen.seeds.redis.RedisAdapter;
import com.github.pingaz.idgen.seeds.redis.RedisSeedRegister;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

/**
 * @author ping
 */
public class CentralSeedTest {

    static class MemorySeedRegister implements SeedRegister{

        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> seedMap = new ConcurrentHashMap<>();

        @Override
        public synchronized int register(String namespace, String seedId) {
            CopyOnWriteArrayList<String> list = seedMap.get(namespace);
            if(list==null){
                CopyOnWriteArrayList<String> value = new CopyOnWriteArrayList<>();
                list = seedMap.putIfAbsent(namespace, value);
                list = list == null ? value : list;
            }

            for(int i=0;i<list.size();i++){
                String ns = list.get(i);
                if(ns==null){
                    list.set(i, seedId);
                    return i;
                }
            }
            return list.add(seedId) ? list.size()-1 : -1;
        }

        @Override
        public int refresh(String namespace, String seedId, int generatorId) {
            CopyOnWriteArrayList<String> list = seedMap.get(namespace);
            if(list == null || generatorId >= list.size()){
                return register(namespace, seedId);
            }

            String sn = list.get(generatorId);
            if(sn == null){
                list.set(generatorId, seedId);
                return generatorId;
            }else if(sn.equals(seedId)){
                return generatorId;
            }else {
                return register(namespace, seedId);
            }
        }

        @Override
        public void unregister(String namespace, String seedId, int generatorId) {
            CopyOnWriteArrayList<String> list = seedMap.get(namespace);
            if(list!=null && list.size() > generatorId){
                list.set(generatorId, null);
            }
        }
    }

    @Test
    public void testRegister(){
        int count = 10000;
        MemorySeedRegister register = new MemorySeedRegister();
        for(int i=0;i<count;i++){
            CentralSeed seed = new CentralSeed(register, "test", "test_"+i, 16 );
            assertEquals(i, seed.getGeneratorId());
        }
    }

    static class MemoryRedisAdapter implements RedisAdapter{
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        @Override
        public String get(String key) {
            return map.get(key);
        }

        @Override
        public boolean set(String key, String value, int seconds) {
            map.put(key, value);
            return true;
        }

        @Override
        public boolean expire(String key, int seconds) {
            return false;
        }

        @Override
        public boolean del(String key) {
            return map.remove(key) != null;
        }
    }
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testRedisSeedRegister(){
        SeedRegister register = new RedisSeedRegister(new MemoryRedisAdapter(), 0, 1023);
        for(int i=0;i<1024;i++){
            CentralSeed seed = new CentralSeed(register, "test", "test_"+i );
            assertEquals(i, seed.getGeneratorId());
        }

        exceptionRule.expect(RuntimeException.class);
        new CentralSeed(register, "test", "test_"+10000);
    }

    @Test
    public void testJedisAdapter(){
        HashSet<HostAndPort> set = new HashSet<>();
        set.add(new HostAndPort("172.18.90.227", 7000));
        set.add(new HostAndPort("172.18.90.227", 7001));
        set.add(new HostAndPort("172.18.90.228", 7000));
        set.add(new HostAndPort("172.18.90.228", 7001));
        SeedRegister register = new RedisSeedRegister(
                new JedisAdapter(set, "YZclskker2sc"), 0, 20);
        for(int i=0;i<=20;i++){
            CentralSeed seed = new CentralSeed(register, "test", "test_"+i, 12 );
            assertEquals(i, seed.getGeneratorId());
        }

        exceptionRule.expect(RuntimeException.class);
        new CentralSeed(register, "test", "test_"+100000, 12);

        for(int i=0;i<=20;i++){
            register.unregister("test", "test_"+i, i);
        }
    }

}