package com.github.pingaz.idgen.seeds;

import com.github.pingaz.idgen.IUID;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ping
 */
public class CentralSeed implements Seed{

    public static final int IPV4_MODE = 0x0f;
    public static final int MAC_MODE = 0x1f;
    public static final int MAC_HASH_MODE = 0x2f;

    private static final Map<String, CentralSeed> seedMap = new ConcurrentHashMap<>(4);

    private static ScheduledExecutorService scheduler;

    public synchronized static void start(long delay, TimeUnit unit){
        if(scheduler != null)
            throw new RuntimeException("The scheduler is started.");

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            for(CentralSeed seed : seedMap.values()){
                seed.refresh();
            }
        }, delay, unit);
    }

    public synchronized void stop(){
        if(scheduler != null && !scheduler.isShutdown()){
            scheduler.shutdown();
        }
    }

    public static CentralSeed getInstance(SeedRegister register, String namespace){
        return getInstance(register, namespace, IPV4_MODE);
    }

    public static CentralSeed getInstance(SeedRegister register, String namespace, int mode){
        return getInstance(register, namespace, mode, 12);
    }

    public static CentralSeed getInstance(SeedRegister register, String namespace, int mode, int bits){
        switch (mode){
            case IPV4_MODE:
                return new CentralSeed(register, namespace, getIpv4(), bits);
            case MAC_MODE:
                return new CentralSeed(register, namespace, getNetworkMac(), bits);
            case MAC_HASH_MODE:
                return new CentralSeed(register, namespace, getNetworkHash(), bits);
            default:
                throw new IllegalArgumentException(String.format(
                        "illegal mode = %d. ", mode
                ));
        }
    }

    private final SeedRegister seedRegister;
    private final String namespace;
    private final String seedId;
    private int generatorId;
    private int generatorBits;

    public CentralSeed(SeedRegister register, String namespace, String seedId, int bits){
        this.seedRegister = register;
        this.namespace = namespace;
        this.seedId = seedId;
        this.generatorBits = bits;
        init();
    }

    public CentralSeed(SeedRegister register, String namespace, String seedId){
        this(register, namespace, seedId, 12);
    }

    private void init(){
        createSeed();
    }

    private void createSeed(){
        this.generatorId = seedRegister.register(namespace, seedId);
        if(this.generatorId < 0){
            throw new RuntimeException(String.format(
                    "Can't create a new generator id for seed [id = %s].", seedId));
        }
        seedMap.putIfAbsent(getNamespace(), this);
    }

    private void refresh(){
        seedRegister.refresh(getNamespace(), getSeedId(), getGeneratorId());
        seedMap.putIfAbsent(getNamespace(), this);
    }

    public String getSeedId() {
        return seedId;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public int getGeneratorId() {
        return this.generatorId;
    }

    @Override
    public int getGeneratorBits() {
        return generatorBits;
    }

    private static String getIpv4(){
        try {
            InetAddress address = InetAddress.getLocalHost();
            byte[] ip = address.getAddress();
            return Integer.toHexString(
                    (((0xff & ip[0])<<24) | ((0xff & ip[1])<<16) | ((0xff & ip[2])<<8) | (0xff & ip[3]))
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getNetworkHash(){
        return Integer.toHexString(IUID.getLocalMachineIdentifier());
    }

    private static String getNetworkMac(){
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(address);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X", mac[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
