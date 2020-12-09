package com.mitmax.backend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.soulwing.snmp.*;

public class SNMPRecord {
    private SnmpContext context;
    private ObservableList<Varbind> varbinds;
    private String ip;

    SNMPRecord(String ip, String community) {
        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress(ip);
        target.setCommunity(community);
        context = SnmpFactory.getInstance().newContext(target, SNMPManager.getMib());

        this.ip = ip;
        varbinds = FXCollections.observableArrayList();
    }

    void retrieve(String... oid) {
        varbinds.addAll(context.getNext(oid).get().asList());
    }

    void close() {
        context.close();
    }

    public ObservableList<Varbind> getVarbinds() {
        return varbinds;
    }

    public String getIp() {
        return ip;
    }
}
