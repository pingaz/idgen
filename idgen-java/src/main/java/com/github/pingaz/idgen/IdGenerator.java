package com.github.pingaz.idgen;

import com.github.pingaz.idgen.seeds.NetworkAddressSeed;
import com.github.pingaz.idgen.seeds.Seed;
import com.github.pingaz.idgen.seeds.SystemPropertySeed;

import java.nio.ByteBuffer;

/**
 * @author ping
 */
public abstract class IdGenerator {

    public static final IdGenerator createIUID(){
        return new IUIDGenerator();
    }

    public static final IdGenerator createLongTimeBased(Seed seed){
        return new LongUIDGenerator(seed.getGeneratorBits(), seed.getGeneratorId());
    }

    public static final IdGenerator createLongTimeBased(int seedId, int seedBits){
        return createLongTimeBased(new SystemPropertySeed(seedId, seedBits));
    }

    public static final IdGenerator createLongTimeBased(int seedId){
        return createLongTimeBased(new SystemPropertySeed(seedId));
    }

    public static final IdGenerator createLongTimeBased(){
        return createLongTimeBased(new NetworkAddressSeed());
    }

    public static final IdGenerator createLongTimeBasedWithIpClassB(){
        return createLongTimeBased(new NetworkAddressSeed(16));
    }

    public static final IdGenerator createLongTimeBasedWithIpClassC(){
        return createLongTimeBased(new NetworkAddressSeed(8));
    }

    protected IdGenerator(){

    }

    public long nextLong(){
        return nextId().toLong();
    }

    public String nextHexString(){
        return nextId().toHexString();
    }

    public byte[] nextBytes(){
        return nextId().toByteArray();
    }

    public ByteBuffer nextByteBuffer(){
        return ByteBuffer.wrap(nextBytes());
    }

    public void nextToByteBuffer(final ByteBuffer buffer) {
        nextId().toByteBuffer(buffer);
    }

    public abstract Id nextId();

}
