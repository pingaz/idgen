package com.github.pingaz.idgen.seeds;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author ping
 */
public class NetworkAddressSeed implements Seed {

    private static final int MAX_GENERATOR_BIT = 24;

    private static int DEFAULT_GENERATOR_ID;

    static{
        try {
            InetAddress address = InetAddress.getLocalHost();
            byte[] ip = address.getAddress();
            DEFAULT_GENERATOR_ID =
                    ((0xff & ip[1])<<16) | ((0xff & ip[2])<<8) | (0xff & ip[3]);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private final int generatorId;
    private final int generatorBits;

    public NetworkAddressSeed() {
        this(12);
    }

    public NetworkAddressSeed(int generatorBits) {
        this( generatorBits, DEFAULT_GENERATOR_ID );
    }

    public NetworkAddressSeed(int generatorBits, int ip){
        if(generatorBits > MAX_GENERATOR_BIT || generatorBits < 0){
            throw new IllegalArgumentException(String.format("generator bits can't be greater than %d or less than 0", MAX_GENERATOR_BIT));
        }
        this.generatorBits = generatorBits;
        this.generatorId = (~(-1 << generatorBits)) & ip;
    }

    @Override
    public int getGeneratorId() {
        return this.generatorId;
    }

    @Override
    public int getGeneratorBits() {
        return this.generatorBits;
    }
}
