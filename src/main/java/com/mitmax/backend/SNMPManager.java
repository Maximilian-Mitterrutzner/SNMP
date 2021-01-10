package com.mitmax.backend;

import com.mitmax.frontend.Controller;
import com.mitmax.frontend.LogLevel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.soulwing.snmp.Mib;
import org.soulwing.snmp.MibFactory;
import org.soulwing.snmp.SnmpFactory;
import org.soulwing.snmp.SnmpListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SNMPManager {
    private static final ObservableMap<String, SNMPTarget> snmpTargets;
    private static final ObservableMap<String, SNMPTarget> pendingSnmpTargets;
    private static final Mib mib;
    private static final AtomicInteger pendingSnmpTargetsCount;
    private static SnmpListener listener;
    private static Runnable onNoMorePendingTargets;

    static {
        snmpTargets = FXCollections.observableHashMap();
        pendingSnmpTargets = FXCollections.observableHashMap();
        mib = MibFactory.getInstance().newMib();
        try {
            for(String module : Settings.mibModules) {
                mib.load(module);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.exit();
        }

        pendingSnmpTargetsCount = new AtomicInteger();
        onNoMorePendingTargets = () -> {};
    }

    public static void scanAddress(String ip, long ipBinary, String community, List<String> oids, boolean isSubnet) {
        new Thread(() -> {
            SNMPTarget target = snmpTargets.get(ip);

            if(target == null) {
                target = pendingSnmpTargets.get(ip);
                if(target == null) {
                    target = new SNMPTarget(ip, ipBinary);
                    pendingSnmpTargets.put(ip, target);
                    pendingSnmpTargetsCount.incrementAndGet();
                }
            }

            target.retrieve(community, oids, isSubnet);
        }).start();
    }

    public static void scanSubnet(String ip, String community, int mask) {
        Thread scanThread = new Thread(() -> {
            if(Settings.logLevel != LogLevel.NONE) {
                Controller.getLogger().logImmediately("Started subnet scan!");
            }

            onNoMorePendingTargets = () -> {
                if(Settings.logLevel != LogLevel.NONE) {
                    Controller.getLogger().logBuffered("Finished subnet scan!");
                    Controller.getLogger().flush();
                }
            };

            int wildcard = 32 - mask;
            long binaryWildcard = (long) (Math.pow(2, wildcard) - 1);
            long netId = (AddressHelper.getAsBinary(ip) >> wildcard) << wildcard;
            long broadcast = netId | binaryWildcard;

            long currentAddress = netId;
            while(++currentAddress != broadcast) {
                scanAddress(AddressHelper.getAsString(currentAddress), currentAddress, community, Settings.initialRequests, true);
            }
        });
        scanThread.setPriority(Thread.MIN_PRIORITY);
        scanThread.start();
    }

    public static void scanRange(long address, long end, String community) {
        if(Settings.logLevel != LogLevel.NONE) {
            Controller.getLogger().logImmediately("Started range scan!");
        }

        onNoMorePendingTargets = () -> {
            if(Settings.logLevel != LogLevel.NONE) {
                Controller.getLogger().logImmediately("Finished range scan!");
            }
        };

        while(address <= end) {
            scanAddress(AddressHelper.getAsString(address), address, community, Settings.initialRequests, true);
            address++;
        }
    }

    public static void closeAll() {
        for(SNMPTarget target : snmpTargets.values()) {
            target.close();
        }

        listener.close();
    }

    public static ObservableMap<String, SNMPTarget> getSnmpTargets() {
        return snmpTargets;
    }

    static Mib getMib() {
        return mib;
    }

    static void onRetrievalDone(SNMPTarget target, boolean successful) {
        pendingSnmpTargets.remove(target.getIp());
        pendingSnmpTargetsCount.decrementAndGet();

        if(successful) {
            Platform.runLater(() -> snmpTargets.put(target.getIp(), target));
        }

        if(pendingSnmpTargetsCount.get() == 0) {
            onNoMorePendingTargets.run();
        }
    }

    public static void registerTrapListener() {
        listener = SnmpFactory.getInstance().newListener(162, SNMPManager.getMib());
        listener.addHandler(event -> {
            Controller.addNotification(event.getSubject());
            return true;
        });
    }
}
