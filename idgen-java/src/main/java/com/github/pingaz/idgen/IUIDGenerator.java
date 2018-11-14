package com.github.pingaz.idgen;

/**
 * @author ping
 */
public class IUIDGenerator extends IdGenerator{

    @Override
    public IUID nextId() {
        return IUID.get();
    }
}
