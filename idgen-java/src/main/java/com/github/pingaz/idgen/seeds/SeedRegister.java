package com.github.pingaz.idgen.seeds;

/**
 * @author ping
 */
public interface SeedRegister {

    int register(String namespace, String seedId);

    int refresh(String namespace, String seedId, int generatorId);

    void unregister(String namespace, String seedId, int generatorId);

}
