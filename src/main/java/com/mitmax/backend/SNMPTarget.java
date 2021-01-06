package com.mitmax.backend;

import javafx.collections.ObservableList;
import org.soulwing.snmp.Varbind;

import java.util.HashMap;
import java.util.List;

public class SNMPTarget  implements Comparable<SNMPTarget> {
    private final String ip;
    private final long ipBinary;
    private final HashMap<String, SNMPRecord> records;
    private String hostName;
    private boolean isAdded;

    SNMPTarget(String ip) {
        this.ip = ip;
        this.ipBinary = AddressHelper.getAsBinary(ip);
        records = new HashMap<>(Settings.communities.size());
        hostName = ip;
        isAdded = false;

        for(String community : Settings.communities) {
            records.put(community, new SNMPRecord(ip, community, this));
        }
    }

    void retrieve(String community, List<String> oids, boolean isSubnet) {
        records.get(community).retrieve(oids, isSubnet);
    }

    void close() {
        for(SNMPRecord record : records.values()) {
            record.close();
        }
    }

    public ObservableList<Varbind> getVarbinds(String community) {
        return records.get(community).getVarbinds();
    }

    void addSelfToList() {
        if(!isAdded) {
            isAdded = true;
            SNMPManager.addTarget(this);
        }
    }

    public String getIp() {
        return ip;
    }

    public String getHostName() {
        return hostName;
    }

    void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public int compareTo(SNMPTarget o) {
        return Long.compare(this.ipBinary, o.ipBinary);
    }
}
