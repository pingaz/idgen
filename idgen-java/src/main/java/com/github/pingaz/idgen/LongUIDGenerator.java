package com.github.pingaz.idgen;


/**
 * @author ping
 */
public class LongUIDGenerator extends IdGenerator {

    private static final int MAX_GENERATOR_BIT = 24;
    private static final int TIMESTAMP_BITS = 35;

    //    private final int generatorBits;
    private final int generatorId;
    private final int generatorLeftShift;
    private final int timestampLeftShift;
    private final int timestampRightShift = 7;
    private final long twepoch = 1466064376616L >> timestampRightShift;

    private final long counterMask;

    private long counter = 0L;
    private long lastTimestamp = -1L;

    public LongUIDGenerator(int generatorBits, int generatorId){
        if (generatorBits > MAX_GENERATOR_BIT || generatorBits < 0){
            throw new IllegalArgumentException(String.format("generator bits can't be greater than %d or less than 0", MAX_GENERATOR_BIT));
        }
        long maxGeneratorId = ~(-1L << generatorBits);
        if (generatorId > maxGeneratorId || generatorId < 0) {
            throw new IllegalArgumentException(String.format("generator Id can't be greater than %d or less than 0", generatorId));
        }
        int counterBits = 64 - TIMESTAMP_BITS - generatorBits - 1;
//        this.generatorBits = generatorBits;
        this.generatorId = generatorId;
        this.timestampLeftShift = counterBits + generatorBits;
        this.generatorLeftShift = counterBits;
        this.counterMask = ~(-1L << counterBits);
    }

    @Override
    public synchronized Id nextId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }else if (lastTimestamp == timestamp) {
            counter = (counter + 1) & counterMask;
            if (counter == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            counter = 0L;
        }

        lastTimestamp = timestamp;

        return new LongId(((timestamp - twepoch) << timestampLeftShift) | (generatorId << generatorLeftShift) | counter);
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis() >> timestampRightShift;
    }

}
