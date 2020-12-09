package com.mitmax.backend;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.soulwing.snmp.Mib;
import org.soulwing.snmp.MibFactory;

import java.io.IOException;

public class SNMPManager {
    private static ObservableList<SNMPRecord> snmpRecords;
    private static Mib mib;

    public static void scanAddress(String ip, String community) {
        SNMPRecord record = new SNMPRecord(ip, community);
        record.retrieve(Settings.initialRequests.toArray(new String[0]));
        getSnmpRecords().add(record);
    }

    public static void closeAll() {
        for(SNMPRecord record : getSnmpRecords()) {
            record.close();
        }
    }

    public static ObservableList<SNMPRecord> getSnmpRecords() {
        if(snmpRecords == null) {
            snmpRecords = FXCollections.observableArrayList();
        }
        return snmpRecords;
    }

    public static Mib getMib() {
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
}
