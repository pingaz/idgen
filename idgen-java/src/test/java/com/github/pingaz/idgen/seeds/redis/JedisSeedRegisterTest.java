package com.github.pingaz.idgen.seeds.redis;

import com.github.pingaz.idgen.IdGenerator;
import com.github.pingaz.idgen.IdGeneratorTest;
import com.github.pingaz.idgen.seeds.CentralSeed;
import com.github.pingaz.idgen.seeds.SeedRegister;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author ping
 */
public class JedisSeedRegisterTest {

    SeedRegister register = new RedisSeedRegister(new JedisAdapter("172.18.20.224", 6379, ""), 0, 20);

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testSimple(){
        String namespace = "testSimple";

        CentralSeed test = CentralSeed.getInstance(register, namespace);
        CentralSeed testMac = CentralSeed.getInstance(register, namespace, CentralSeed.MAC_MODE);
        CentralSeed seed = new CentralSeed(register, namespace, "test");

        try {
            assertEquals(12, test.getGeneratorBits());
            assertEquals(0, test.getGeneratorId());

            assertEquals(12, testMac.getGeneratorBits());
            assertEquals(1, testMac.getGeneratorId());

            assertEquals(12, seed.getGeneratorBits());
            assertEquals(2, seed.getGeneratorId());
        }finally {
            register.unregister(namespace, test.getSeedId(), test.getGeneratorId());
            register.unregister(namespace, testMac.getSeedId(), testMac.getGeneratorId());
            register.unregister(namespace, seed.getSeedId(), seed.getGeneratorId());

        }
    }

    @Test
    public void testScheduler() throws InterruptedException {
        SeedRegister register = new RedisSeedRegister(
                new JedisAdapter("172.18.20.224", 6379, ""),
                0, 20, 5);

        String namespace = "testScheduler";
        CentralSeed test = CentralSeed.getInstance(register, namespace);

        CentralSeed.start(2, TimeUnit.SECONDS);
        Thread.sleep(5 * 1000);
        CentralSeed test2 = CentralSeed.getInstance(register, namespace, CentralSeed.MAC_MODE);
        assertNotEquals(test.getGeneratorId(), test2.getGeneratorId());
        register.unregister(namespace, test2.getSeedId(), test2.getGeneratorId());
        CentralSeed.stop();

        Thread.sleep(3 * 1000);
        CentralSeed.start(2, TimeUnit.SECONDS);
        CentralSeed test3 = CentralSeed.getInstance(register, namespace);
        assertEquals(test.getGeneratorId(), test3.getGeneratorId());
    }

    @Test
    public void testTooMuchSeed(){
        int edge = 21;
        System.out.println(String.format("Test count was %d.", edge));
        String namespace = "testTooMuchSeed";
        List<CentralSeed> list = new ArrayList<>();
        for(int i = 0; i<edge; i++){
            long time = System.currentTimeMillis();
            CentralSeed test = new CentralSeed(register, namespace, "seed_"+i, 12);
            assertEquals(12, test.getGeneratorBits());
            assertEquals(i, test.getGeneratorId());
            System.out.println(String.format("Tested %d, used %s seconds.", i, ((double)System.currentTimeMillis()-time)/1000));
            list.add(test);
        }

        exceptionRule.expect(RuntimeException.class);
        new CentralSeed(register, namespace, "seed_"+edge, 12);

        for(int i=0; i<Math.max(5, (int)(edge * Math.random()/10)); i++){
            long time = System.currentTimeMillis();
            int seedIndex = (int) (Math.random() * edge);
            CentralSeed test = new CentralSeed(register, namespace, "seed_"+seedIndex);
            assertEquals(12, test.getGeneratorBits());
            assertEquals(seedIndex, test.getGeneratorId());
            System.out.println(String.format("Tested %d, used %s seconds.", seedIndex, ((double)System.currentTimeMillis()-time)/1000));
        }

        System.out.println(String.format("Starting delete keys, count[%d].", edge));
        for(CentralSeed seed: list){
            register.unregister(namespace, seed.getSeedId(), seed.getGeneratorId());
        }
    }

    @Test
    public void testLongGenPerformance(){
        String namespace = "testLongGeneratorPerformance";
        CentralSeed seed = CentralSeed.getInstance(register, namespace);
        IdGenerator idGenerator = IdGenerator.createLongTimeBased(seed);

        int number = 1000 * 1000;
        long time = System.currentTimeMillis();
        long count = 0;
        for(int i=0;i<number;i++){
            long id = idGenerator.nextLong();
            long oldCount = count;
            count = id % (id >> 18 << 18);
            assertTrue((count == 0 || count == oldCount+1));
        }
        register.unregister(namespace, seed.getSeedId(), seed.getGeneratorId());
        System.out.println("Performance: "+ ((double)System.currentTimeMillis() - time)/1000 + " sec per milo.");
    }

    @Test
    public void testLongGenerator() {
        String ns = "testLongGenerator";
        CentralSeed seed = CentralSeed.getInstance(register, ns);
        IdGenerator idGenerator = IdGenerator.createLongTimeBased(seed);
        validateLongGenerator(seed.getGeneratorId(), seed.getGeneratorBits(), idGenerator);
        register.unregister(ns, seed.getSeedId(), seed.getGeneratorId());
    }

    private void validateLongGenerator(int generator, int generatorBit, IdGenerator idGenerator) {
        IdGeneratorTest.validateLongGenerate(generator, generatorBit, idGenerator);
    }

    private void validateLongGenerate(final int generator, final int generatorBit, IdGenerator idGenerator) {
        IdGeneratorTest.validateLongGenerate(generator, generatorBit, idGenerator);
    }
}