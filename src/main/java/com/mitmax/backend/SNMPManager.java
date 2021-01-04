package com.mitmax.backend;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.soulwing.snmp.Mib;
import org.soulwing.snmp.MibFactory;

import java.io.IOException;

public class SNMPManager {
    private static final ObservableMap<String, SNMPTarget> snmpTargets;
    private static final Mib mib;

    static {
        snmpTargets = FXCollections.observableHashMap();
        mib = MibFactory.getInstance().newMib();
        try {
            for(String module : Settings.mibModules) {
                mib.load(module);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.exit();
        }
    }

    public static void scanAddress(String ip, String community) {
        new Thread(() -> {
            SNMPTarget target = snmpTargets.get(ip);

            if(target == null) {
                target = new SNMPTarget(ip);
            }

            target.retrieve(community, Settings.initialRequests);
        }).start();
    }

    public static void scanSubnet(String ip, String community, int mask) {
        Thread scanThread = new Thread(() -> {
            int wildcard = 32 - mask;
            long binaryWildcard = (long) (Math.pow(2, wildcard) - 1);
            long netId = (AddressHelper.getAsBinary(ip) >> wildcard) << wildcard;
            long broadcast = netId | binaryWildcard;

            long currentAddress = netId;
            while(++currentAddress != broadcast) {
                scanAddress(AddressHelper.getAsString(currentAddress), community);
            }
        });
        scanThread.setPriority(Thread.MIN_PRIORITY);
        scanThread.start();
    }

    public static void closeAll() {
        for(SNMPTarget target : snmpTargets.values()) {
            target.close();
        }
    }

    public static ObservableMap<String, SNMPTarget> getSnmpTargets() {
        return snmpTargets;
    }

    static Mib getMib() {
        return mib;
    }

    static void addTarget(SNMPTarget target) {
        snmpTargets.put(target.getIp(), target);
    }
}
