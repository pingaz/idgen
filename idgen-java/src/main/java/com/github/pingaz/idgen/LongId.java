package com.github.pingaz.idgen;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author ping
 */
public class LongId extends Id {

    private long value;

    public LongId(long value) {
        this.value = value;
    }

    @Override
    public long toLong(){
        return value;
    }

    @Override
    public byte[] toByteArray() {
        return toByteBuffer().array();
    }

    @Override
    public ByteBuffer toByteBuffer(){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(this.value);
        return buffer;
    }

    public void toByteBuffer(final ByteBuffer buffer) {
        if(buffer == null){
            throw new NullPointerException();
        }
        if(buffer.remaining() < 8){
            throw new IllegalArgumentException("buffer.remaining() < " + 8);
        }
        buffer.putLong(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongId longId = (LongId) o;
        return value == longId.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return "LongId{" +
                "value=" + value +
                '}';
    }
}
