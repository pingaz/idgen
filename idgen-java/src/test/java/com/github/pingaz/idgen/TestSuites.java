package com.github.pingaz.idgen;

import com.github.pingaz.idgen.seeds.CentralSeedTest;
import com.github.pingaz.idgen.seeds.NetworkAddressSeedTest;
import com.github.pingaz.idgen.seeds.redis.JedisSeedRegisterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author ping
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        IdGeneratorTest.class,
        NetworkAddressSeedTest.class,
        CentralSeedTest.class,
        JedisSeedRegisterTest.class
})
public class TestSuites {
}
