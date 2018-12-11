package com.github.pingaz.idgen.seeds;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ping
 */
public class NetworkAddressSeedTest {

    @Test
    public void testNetwork(){
        NetworkAddressSeed networkAddressSeed = new NetworkAddressSeed();
        System.out.println(networkAddressSeed.getGeneratorId());
    }

    @Test
    public void testConstruct(){
        for(int i=0;i<=24;i++){
            validate(i);
            System.out.println("tested at bits: "+i);
        }
    }

    private void validate(int bits) {
        int max = ~(-1<<bits);
        for(int i=0; i < ~(-1<<24); i++){
            NetworkAddressSeed networkAddressSeed = new NetworkAddressSeed(bits, i);
            assertTrue(
                    "Error at " + i + ", gen " + networkAddressSeed.getGeneratorId() + ", max "+max,
                    networkAddressSeed.getGeneratorId() == i ||
                            networkAddressSeed.getGeneratorId() <= max ||
                            networkAddressSeed.getGeneratorId()==0);
        }
    }
}