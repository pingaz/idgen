package com.github.pingaz.idgen;

import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ping
 */
public class IUID extends Id implements Comparable<IUID>{
    private static final int HIGH_TIMESTAMP_TWO_BYTES = 0x0000ffff;
    private static final char[] HEX_CHARS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static int MACHINE_IDENTIFIER;
    private static short PROCESS_IDENTIFIER;
    private static final AtomicInteger NEXT_COUNTER = new AtomicInteger(new SecureRandom().nextInt());

    /**
     * Gets a new BytesTUID.
     *
     * @return the new id
     */
    public static IUID get() {
        return new IUID();
    }

    /**
     * Gets a BytesTUID.
     *
     * @return the new id
     */
    public static IUID getByHexString(String hexString) {
        return new IUID(parseHexString(hexString));
    }


    /**
     * Checks if a string could be an {@code BytesTUID}.
     *
     * @param hexString a potential BytesTUID as a String.
     * @return whether the string could be a node id
     * @throws IllegalArgumentException if hexString is null
     */
    public static boolean isValid(final String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException();
        }

        int len = hexString.length();
        if (len != 32) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            char c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                continue;
            }
            if (c >= 'A' && c <= 'F') {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Gets the current value of the auto-incrementing counter.
     *
     * @return the current counter value.
     */
    public static int getCurrentCounter() {
        return NEXT_COUNTER.get();
    }

    private final int highTimestamp;
    private final int lowTimestamp;
    private final int machineIdentifier;
    private final short processIdentifier;
    private final int counter;


    public IUID(){
        this(new Date());
    }

    public IUID(final Date date){
        this(date.getTime(), MACHINE_IDENTIFIER, PROCESS_IDENTIFIER, NEXT_COUNTER.getAndIncrement());
    }

    public IUID(long timestamp, int machineIdentifier, short processIdentifier, int counter) {
        this.highTimestamp = (int)(timestamp >> 32) & HIGH_TIMESTAMP_TWO_BYTES;
        this.lowTimestamp = (int) timestamp;
        this.machineIdentifier = machineIdentifier;
        this.processIdentifier = processIdentifier;
        this.counter = counter;
    }

    public IUID(final byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public IUID(ByteBuffer buffer){
        if(buffer == null){
            throw new NullPointerException();
        }
        if(buffer.remaining() < 16){
            throw new IllegalArgumentException("buffer.remaining() < 16");
        }

        // Note: Cannot use ByteBuffer.getInt because it depends on tbe buffer's byte order
        // and NodeId's are always in big-endian order.
        highTimestamp = makeInt((byte) 0, (byte) 0, buffer.get(), buffer.get());
        lowTimestamp = makeInt(buffer.get(), buffer.get(), buffer.get(), buffer.get());
        machineIdentifier = makeInt(buffer.get(), buffer.get(), buffer.get(), buffer.get());
        processIdentifier = (short) makeInt((byte) 0, (byte) 0, buffer.get(), buffer.get());
        counter = makeInt(buffer.get(), buffer.get(), buffer.get(), buffer.get());
    }

    public long getTimestamp() {
        return ((long)highTimestamp<<32) | (lowTimestamp & 0x0l);
    }

    public int getMachineIdentifier() {
        return machineIdentifier;
    }

    public short getProcessIdentifier() {
        return processIdentifier;
    }

    public int getCounter() {
        return counter;
    }

    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(16);
        putToByteBuffer(buffer);
        return buffer.array();
    }

    public void putToByteBuffer(final ByteBuffer buffer) {
        if(buffer == null){
            throw new NullPointerException();
        }
        if(buffer.remaining() < 16){
            throw new IllegalArgumentException("buffer.remaining() < 16");
        }

        buffer.put(int1(highTimestamp));
        buffer.put(int0(highTimestamp));
        buffer.put(int3(lowTimestamp));
        buffer.put(int2(lowTimestamp));
        buffer.put(int1(lowTimestamp));
        buffer.put(int0(lowTimestamp));
        buffer.put(int3(machineIdentifier));
        buffer.put(int2(machineIdentifier));
        buffer.put(int1(machineIdentifier));
        buffer.put(int0(machineIdentifier));
        buffer.put(short1(processIdentifier));
        buffer.put(short0(processIdentifier));
        buffer.put(int3(counter));
        buffer.put(int2(counter));
        buffer.put(int1(counter));
        buffer.put(int0(counter));
    }

    /**
     * Converts this instance into a 32-byte hexadecimal string representation.
     *
     * @return a string representation of the NodeId in hexadecimal format
     */
    public String toHexString() {
        char[] chars = new char[32];
        int i = 0;
        for (byte b : toByteArray()) {
            chars[i++] = HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IUID id = (IUID) o;
        return highTimestamp == id.highTimestamp &&
                lowTimestamp == id.lowTimestamp &&
                machineIdentifier == id.machineIdentifier &&
                processIdentifier == id.processIdentifier &&
                counter == id.counter;
    }

    @Override
    public int hashCode() {
        int result = highTimestamp;
        result = 31 * result + lowTimestamp;
        result = 31 * result + machineIdentifier;
        result = 31 * result + (int) processIdentifier;
        result = 31 * result + counter;
        return result;
    }

    //@Override
    public int compareTo(IUID other) {
        if (other == null) {
            throw new NullPointerException();
        }

        byte[] byteArray = toByteArray();
        byte[] otherByteArray = other.toByteArray();
        for (int i = 0; i < 16; i++) {
            if (byteArray[i] != otherByteArray[i]) {
                return ((byteArray[i] & 0xff) < (otherByteArray[i] & 0xff)) ? -1 : 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return toHexString();
    }

    static {
        try {
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int createMachineIdentifier() {
        // build a 2-byte machine piece based on NICs info
        int machinePiece;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                sb.append(ni.toString());
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    } catch (BufferUnderflowException shortHardwareAddressException) { //NOPMD
                        // mac with less than 6 bytes. continue
                    }
                }
            }
            machinePiece = sb.toString().hashCode();
        } catch (Throwable t) {
            // exception sometimes happens with IBM JVM, use SecureRandom instead
            machinePiece = (new SecureRandom().nextInt());
            //LOGGER.debug("Failed to get machine identifier from network interface, using SecureRandom instead");
        }
        return machinePiece;
    }

    // Creates the process identifier.  This does not have to be unique per class loader because
    // NEXT_COUNTER will provide the uniqueness.
    private static short createProcessIdentifier() {
        short processId;
        try {
            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@")) {
                processId = (short) Integer.parseInt(processName.substring(0, processName.indexOf('@')));
            } else {
                processId = (short) java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
            }

        } catch (Throwable t) {
            // JMX not available on Android, use SecureRandom instead
            processId = (short) new SecureRandom().nextInt();
            //LOGGER.debug("Failed to get process identifier from JMX, using SecureRandom instead");
        }

        return processId;
    }

    private static byte[] parseHexString(final String s) {
        if (!isValid(s)) {
            throw new IllegalArgumentException("invalid hexadecimal representation of an TaskId: [" + s + "]");
        }

        byte[] b = new byte[16];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }

    private static byte int3(final int x) {
        return (byte) (x >> 24);
    }

    private static byte int2(final int x) {
        return (byte) (x >> 16);
    }

    private static byte int1(final int x) {
        return (byte) (x >> 8);
    }

    private static byte int0(final int x) {
        return (byte) (x);
    }

    private static byte short1(final short x) {
        return (byte) (x >> 8);
    }

    private static byte short0(final short x) {
        return (byte) (x);
    }

    private static int makeInt(final byte b3, final byte b2, final byte b1, final byte b0) {
        // CHECKSTYLE:OFF
        return (((b3) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) << 8) |
                ((b0 & 0xff)));
        // CHECKSTYLE:ON
    }
}
