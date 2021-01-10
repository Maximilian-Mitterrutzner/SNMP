package com.mitmax.backend;

import com.mitmax.frontend.Controller;
import com.mitmax.frontend.LogLevel;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.soulwing.snmp.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class SNMPRecord {
    private final SnmpContext context;
    private final ObservableMap<String, Varbind> varbindsMap;
    private final ObservableList<Varbind> varbindsList;
    private final String ip;
    private final String community;
    private final SNMPTarget parent;
    private final AtomicInteger pendingRequests;

    SNMPRecord(String ip, String community, SNMPTarget parent) {
        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress(ip);
        target.setCommunity(community);
        SimpleSnmpTargetConfig config = new SimpleSnmpTargetConfig();
        config.setTimeout(Settings.timeout / 2);
        config.setRetries(1);
        context = SnmpFactory.getInstance().newContext(target, SNMPManager.getMib(), config, null);

        varbindsMap = FXCollections.observableMap(new HashMap<>());
        varbindsList = FXCollections.observableArrayList();
        varbindsMap.addListener((MapChangeListener<String, Varbind>) change -> {
            if(change.wasRemoved()) {
                varbindsList.remove(change.getValueRemoved());
            }
            if(change.wasAdded()) {
                varbindsList.add(change.getValueAdded());
            }
        });
        this.ip = ip;
        this.community = community;
        this.parent = parent;
        this.pendingRequests = new AtomicInteger();
    }

    void retrieve(List<String> oids, boolean isSubnet) {
        pendingRequests.incrementAndGet();

        if(!isSubnet && Settings.logLevel != LogLevel.NONE) {
            Controller.getLogger().logImmediately("Sent request! (" + ip + " - " + community + " - " + oids + ")");
        }

        try {
            context.asyncGetNext(event -> {
                try {
                    VarbindCollection received = event.getResponse().get();
                    for(Varbind varbind : received) {
                        if(varbind.getOid().equals("1.3.6.1.2.1.1.5.0") || varbind.getName().equals("sysName.0")) {
                            parent.setHostName(varbind.asString() + " (" + ip + ")");
                            Controller.refreshListView();
                        }

                        synchronized (varbindsMap) {
                            varbindsMap.put(varbind.getOid(), varbind);
                        }
                    }
                    pendingRequests.decrementAndGet();
                    parent.onRetrievalDone(true);
                    return;
                } catch (TimeoutException ex) {
                    if(Settings.logLevel == LogLevel.FULL
                    || (Settings.logLevel == LogLevel.SOME && !isSubnet)) {
                        Controller.getLogger().logBuffered("Request timed out! (" + ip + " - " + community + " - " + oids.toString() + ")");
                    }
                } catch (SnmpException ex) {
                    if(Settings.logLevel != LogLevel.NONE) {
                        Controller.getLogger().logImmediately(ex.getMessage().split(": ")[2]);
                    }
                } catch (Exception ex) {
                    System.out.println("Unexpected Exception: ");
                    ex.printStackTrace();
                }
                pendingRequests.decrementAndGet();
                parent.onRetrievalDone(false);
            }, oids);
        } catch (IllegalArgumentException ex) {
            if(Settings.logLevel != LogLevel.NONE) {
                Controller.getLogger().logImmediately("OID " + ex.getMessage());
            }
            pendingRequests.decrementAndGet();
            parent.onRetrievalDone(false);
        }
    }

    void close() {
        context.close();
    }

    ObservableList<Varbind> getVarbinds() {
        return varbindsList;
    }

    AtomicInteger getPendingRequests() {
        return pendingRequests;
    }
}
