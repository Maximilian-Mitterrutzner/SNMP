package com.mitmax.backend;

import javafx.collections.ObservableList;
import org.soulwing.snmp.Varbind;

import java.util.HashMap;

public class SNMPTarget {
    private String ip;
    private HashMap<String, SNMPRecord> records;

    SNMPTarget(String ip) {
        this.ip = ip;
        records = new HashMap<>();

        for(String community : Settings.communities) {
            records.put(community, new SNMPRecord(ip, community, this));
        }
    }

    void retrieve(String community, String... oid) {
        records.get(community).retrieve(oid);
    }

    void close() {
        for(SNMPRecord record : records.values()) {
            record.close();
        }
    }

    public ObservableList<Varbind> getVarbinds(String community) {
        return records.get(community).getVarbinds();
    }

    void removeIfEmpty() {
        for(SNMPRecord record : records.values()) {
            if(record.getVarbinds().size() == 0 && record.getPendingRequests() == 0) {
                SNMPManager.removeTarget(this);
            }
        }
    }

    public String getIp() {
        return ip;
    }
}
