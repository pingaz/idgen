package com.github.pingaz.idgen;

import com.github.pingaz.idgen.seeds.NetworkAddressSeed;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author ping
 */
public class IdGeneratorTest {

    @Test
    public void testHexString(){
        IdGenerator idGenerator = IdGenerator.createLongTimeBased();
        byte[] bytes = idGenerator.nextBytes();
        assertEquals(8, bytes.length);
        assertEquals(16, idGenerator.nextHexString().length());
        assertEquals(8, idGenerator.nextByteBuffer().array().length);
    }

    @org.junit.Test
    public void testLongGeneratorPerformance(){
        NetworkAddressSeed seed = new NetworkAddressSeed(10);
        IdGenerator idGenerator = IdGenerator.createLongTimeBased(seed);

        int number = 1000 * 1000;
        long time = System.currentTimeMillis();
        long count = 0;
        HashSet<Id> idSet = new HashSet<>();
        for(int i=0;i<number;i++){
            Id longId = idGenerator.nextId();
            long id = longId.toLong();
            long oldCount = count;
            count = id % (id >> 18 << 18);
            long generatorId = (id >> 18) % (id >> 28 << 10);
            assertTrue((count == 0 || count == oldCount+1));
            assertEquals(seed.getGeneratorId(), generatorId);
            assertTrue(idSet.add(longId));
        }
        System.out.println("Performance: "+ ((double)System.currentTimeMillis() - time)/1000 + " sec per milo.");
    }

    @org.junit.Test
    public void testLongGeneratorByNetwork() throws InterruptedException {
        NetworkAddressSeed generator = new NetworkAddressSeed(8);
        IdGenerator idGenerator = IdGenerator.createLongTimeBasedWithIpClassC();
        validateLongGenerator(generator.getGeneratorId(), generator.getGeneratorBits(), idGenerator);
    }

    @org.junit.Test
    public void testLongGenerator() throws InterruptedException {
        int generator = 10;
        IdGenerator idGenerator = IdGenerator.createLongTimeBased(generator);
        validateLongGenerator(generator, 10, idGenerator);
        assertNotEquals(idGenerator.nextId(), idGenerator.nextId());
    }

    public static void validateLongGenerator(int generator, int generatorBit, IdGenerator idGenerator) throws InterruptedException {
        long id = idGenerator.nextLong();
        int generatorLeftShift = 28 - generatorBit;

        long timestamp = (id >> 28 << 7) + 1466064376616L;
        long generatorId = (id >> generatorLeftShift) % (id >> 28 << generatorBit);
        long counter = id % (id >> generatorLeftShift << generatorLeftShift);
        System.out.println("timestamp: " + timestamp + ", now: " + System.currentTimeMillis());
        System.out.println("date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)));
        System.out.println("counter: "+counter);
        System.out.println("generator Id: "+generatorId);
        assertTrue((System.currentTimeMillis()-timestamp)<200);
        assertEquals(0, counter);
        assertEquals(generator, generatorId);

        id = idGenerator.nextLong();
        long oldCounter = counter;
        counter = id % (id >> generatorLeftShift << generatorLeftShift);
        System.out.println("counter: "+counter);
        assertTrue(counter == 0 || counter == oldCounter+1);

        id = idGenerator.nextLong();
        oldCounter = counter;
        counter = id % (id >> generatorLeftShift << generatorLeftShift);
        System.out.println("counter: "+counter);
        assertTrue(counter == 0 || counter == oldCounter+1);

        id = idGenerator.nextLong();
        oldCounter = counter;
        counter = id % (id >> generatorLeftShift << generatorLeftShift);
        System.out.println("counter: "+counter);
        assertTrue(counter == 0 || counter == oldCounter+1);

        Thread.sleep(128);
        id = idGenerator.nextLong();
        counter = id % (id >> generatorLeftShift << generatorLeftShift);
        System.out.println("counter: "+counter);
        assertEquals(0, counter);

        validateLongGenerate(generator, generatorBit, idGenerator);
    }

    public  static void validateLongGenerate(final int generator, final int generatorBit, IdGenerator idGenerator) {
        ConcurrentHashMap<Long, Long> set = new ConcurrentHashMap<>();

        int generatorLeftShift = 28 - generatorBit;
        AtomicInteger counter = new AtomicInteger(0);
        int threadNumber = 20;
        for(int i = 0; i< threadNumber; i++)
            new Thread(() -> {
                int count = 10000;
                long threadId = Thread.currentThread().getId();
                long timestamp = System.currentTimeMillis();
                for(int i1 = 0; i1 <count; i1++){
                    long value = idGenerator.nextLong();
                    assertEquals(generator, (value >> generatorLeftShift) % (value >> 28 << generatorBit));
                    assertTrue("Duplication id generated.", set.putIfAbsent(value, threadId) == null);
                }
                double time = System.currentTimeMillis() - timestamp;
                System.out.println("Thread - " + threadId + ": time: " + (1000*time/count) + " seconds per milo." +
                        " Id - " + idGenerator.nextId());
                counter.incrementAndGet();
            }).start();

        while(true){
            try {
                Thread.sleep(1000);
                if(counter.get() == threadNumber){
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}