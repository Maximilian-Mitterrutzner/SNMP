package com.mitmax.backend;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.soulwing.snmp.Mib;
import org.soulwing.snmp.MibFactory;

import java.io.IOException;

public class SNMPManager {
    private static ObservableList<SNMPTarget> snmpTargets;
    private static Mib mib;

    public static void scanAddress(String ip, String community) {
        SNMPTarget target = null;

        for(SNMPTarget currentTarget : getSnmpTargets()) {
            if(currentTarget.getIp().equals(ip)) {
                target = currentTarget;
                break;
            }
        }

        if(target == null) {
            target = new SNMPTarget(ip);
            getSnmpTargets().add(target);
        }

        target.retrieve(community, Settings.initialRequests.toArray(new String[0]));
    }

    public static void closeAll() {
        for(SNMPTarget target : getSnmpTargets()) {
            target.close();
        }
    }

    public static ObservableList<SNMPTarget> getSnmpTargets() {
        if(snmpTargets == null) {
            snmpTargets = FXCollections.observableArrayList();
        }
        return snmpTargets;
    }

    static Mib getMib() {
        if(mib == null) {
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
        return mib;
    }

    static void removeTarget(SNMPTarget target) {
        Platform.runLater(() -> getSnmpTargets().remove(target));
    }
}
