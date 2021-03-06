package com.github.pingaz.idgen.seeds.redis;

import com.github.pingaz.idgen.seeds.SeedRegister;

/**
 * @author ping
 */
public class RedisSeedRegister implements SeedRegister {

    public static final int DEFAULT_BITS = 12;
    public static final int DEFAULT_EXPIRED = 7 * 24 * 60 * 60;

    private final RedisAdapter redis;
    private final int from;
    private final int to;
    private final int expired;

    public RedisSeedRegister(RedisAdapter redis, int from, int to, int expired) {
        this.redis = redis;
        this.from = from;
        this.to = to;
        this.expired = expired;
    }

    public RedisSeedRegister(RedisAdapter redis, int from, int to) {
        this(redis, from, to, DEFAULT_EXPIRED);
    }

    public RedisSeedRegister(RedisAdapter redis, int bits) {
        this(redis, 0, (1<<bits)-1);
    }

    public RedisSeedRegister(RedisAdapter redis) {
        this(redis, DEFAULT_BITS);
    }

    @Override
    public int register(String namespace, String seedId) {
        String indexKey = getIndexKey(namespace, seedId);
        String index = redis.get(indexKey);
        System.out.println("register: index key - "+indexKey + ", index - "+index);
        if(index != null){
            return getGeneratorId(index);
        }else{
            for(int i=from; i<=to; i++){
                String key = getSeedKey(namespace, i);
                String value = redis.get(key);
                if(value==null){
                    boolean reply = redis.set(key, seedId, getExpire());
                    if(reply){
                        redis.set(indexKey, getGeneratorHex(i), getExpire());
                        return i;
                    }
                }else if(value.equals(seedId)){
                    return i;
                }
            }
            return -1;
        }
    }

    @Override
    public int refresh(String namespace, String seedId, int generatorId) {
        String seedKey = getSeedKey(namespace, generatorId);
        String oldSeedId = redis.get(seedKey);
        if(oldSeedId!=null && oldSeedId.equals(seedId)){
            redis.expire(seedKey, getExpire());
            redis.expire(getIndexKey(namespace, seedId), getExpire());
            return generatorId;
        }else{
            return register(namespace, seedId);
        }
    }

    @Override
    public void unregister(String namespace, String seedId, int generatorId) {
        String indexKey = getIndexKey(namespace, seedId);
        String seedKey = getSeedKey(namespace, generatorId);
        String oldSeedId = redis.get(seedKey);
        if(oldSeedId ==null || oldSeedId.equals(seedId)){
            redis.del(indexKey);
        }
        redis.del(seedKey);
    }

    protected int getExpire() {
        return expired;
    }

    protected int getGeneratorId(String index) {
        return Integer.parseInt(index, 16);
    }
    protected String getGeneratorHex(int index) {
        return Integer.toHexString(index);
    }

    protected String getIndexKey(String namespace, String seedId){
        return new StringBuilder(namespace).append(":index:").append(seedId).toString();
    }

    protected String getSeedKey(String namespace, int index){
        return new StringBuilder(namespace).append(":seed:").append(Integer.toHexString(index)).toString();
    }
}
