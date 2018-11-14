package com.github.pingaz.idgen.seeds;

/**
 * @author ping
 */
public class SystemPropertySeed implements Seed {

    private static final int BITS = Integer.parseInt(System.getProperty("idgen.bits", "10"));
    private static final int ID = Integer.parseInt(System.getProperty("idgen.gen", "0"));

    private final int generatorId;

    private final int generatorBits;

    public SystemPropertySeed() {
        this(ID, BITS);
    }

    public SystemPropertySeed(int generatorId) {
        this(generatorId, BITS);
    }

    public SystemPropertySeed(int generatorId, int generatorBits) {
        this.generatorId = generatorId;
        this.generatorBits = generatorBits;
    }

    @Override
    public int getGeneratorId() {
        return generatorId;
    }

    @Override
    public int getGeneratorBits() {
        return generatorBits;
    }
}
