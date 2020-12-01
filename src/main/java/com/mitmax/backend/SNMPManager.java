package com.mitmax.backend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SNMPManager {
    public static ObservableList<SNMPRecord> snmpRecords = FXCollections.observableArrayList();

    public static void scanAddress(String ip, String community) {
        SNMPRecord record = new SNMPRecord(ip, community);
        record.retrieve(Settings.initialRequests.toArray(new String[0]));
        snmpRecords.add(record);
    }

    public static void closeAll() {
        for(SNMPRecord record : snmpRecords) {
            record.close();
        }
    }
}
