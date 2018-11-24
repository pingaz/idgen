package com.github.pingaz.idgen.seeds.redis;

import com.github.pingaz.idgen.seeds.SeedRegister;

/**
 * @author ping
 */
public class RedisSeedRegister implements SeedRegister {

    public static final int DEFAULT_BITS = 12;
    public static final int SECONDS_PER_WEEK = 7 * 24 * 60 * 60;

    private final RedisAdapter redis;
    private final int from;
    private final int to;

    public RedisSeedRegister(RedisAdapter redis, int from, int to) {
        this.redis = redis;
        this.from = from;
        this.to = to;
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
        if(index != null){
            int generatorId = getGeneratorId(index);
            return refresh(namespace, seedId, generatorId);
        }else{
            for(int i=from; i<=to; i++){
                String key = getSeedKey(namespace, i);
                String value = redis.get(key);
                if(value==null){
                    boolean reply = redis.set(key, seedId, getExpire());
                    if(reply){
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
        String seedKey = getSeedKey(namespace, generatorId);
        String oldSeedId = redis.get(seedKey);
        if(oldSeedId!=null && oldSeedId.equals(seedId)){
            redis.del(oldSeedId);
        }
        redis.del(seedKey);
    }

    protected int getExpire() {
        return SECONDS_PER_WEEK;
    }

    protected int getGeneratorId(String index) {
        return Integer.parseInt(index, 16);
    }

    protected String getIndexKey(String namespace, String seedId){
        return new StringBuilder(namespace).append(":index:").append(seedId).toString();
    }

    protected String getSeedKey(String namespace, int index){
        return new StringBuilder(namespace).append(":seed:").append(Integer.toHexString(index)).toString();
    }
}
