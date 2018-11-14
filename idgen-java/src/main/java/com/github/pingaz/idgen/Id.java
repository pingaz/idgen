package com.github.pingaz.idgen;

import java.nio.ByteBuffer;

/**
 * @author ping
 */
public abstract class Id {
    public abstract byte[] toByteArray();

    public long toLong(){
        byte[] byteArray = toByteArray();
        if(byteArray.length > 8){
            throw new IllegalArgumentException("Can't convert a byte array to long, because it's length more than 8");
        }

        long id = 0l;
        int bit = 0;
        for(byte b : byteArray){
            id |= (b & 0xff) << bit;
            bit += 8;
        }
        return id;
    }

    public String toHexString(){
        byte[] byteArray = toByteArray();
        char[] chars = new char[byteArray.length * 2];
        int i = 0;
        for (byte b : byteArray) {
            chars[i++] = HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }

    public ByteBuffer toByteBuffer(){
        return ByteBuffer.wrap(toByteArray());
    }

    public void toByteBuffer(final ByteBuffer buffer) {
        if(buffer == null){
            throw new NullPointerException();
        }
        byte[] byteArray = toByteArray();
        if(buffer.remaining() < byteArray.length){
            throw new IllegalArgumentException("buffer.remaining() < " + byteArray.length);
        }
        buffer.put(byteArray);
    }

    private static final char[] HEX_CHARS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
}
