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

/**
 * This class represents a single endpoint specified by an IP-address and a community.
 * Here the actual SNMP-request is performed.
 * This class attempts to keep track of how many requests are still pending to decide whether to remove itself
 * from the {@link ObservableMap} of pending requests in {@link SNMPManager}
 * and whether to add itself to the {@link ObservableMap} of successful requests (also in {@link SNMPManager}).
 */
class SNMPRecord {
    private final SnmpContext context;
    private final ObservableMap<String, Varbind> varbindsMap;
    private final ObservableList<Varbind> varbindsList;
    private final String ip;
    private final String community;
    private final SNMPTarget parent;
    private final AtomicInteger pendingRequests;

    /**
     * Constructs a new instance of this class.
     * @param ip a {@code String} containing the IP-address to send requests to.
     * @param community a {@code String} containing the community to perform requests in.
     * @param parent the {@link SNMPTarget} containing the parent,
     *               i.e. a container for all {@link SNMPRecord}s of a single IP-address.
     */
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

    /**
     * Retrieve a list of {@link org.snmp4j.smi.OID}s in {@code String} format.
     * If the received information contains the Hostname of the device, it will be set
     * and the {@link javafx.scene.control.ListView} containing the device-list will be updated.
     * Will log additional information if so specified by the application's {@link LogLevel}.
     * The amount of logs is also influenced by {@code isSubnet}:
     * If {@code true}, less detailed information will be logged.
     * @param oids a {@link List} containing {@link org.snmp4j.smi.OID}s to request.
     * @param isSubnet a {@code boolean} specifying whether this action was performed
     *                 as a part of a larger subnet- or range-scan.
     */
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

    /**
     * Closes the opened context.
     */
    void close() {
        context.close();
    }

    /**
     * Getter for the {@link ObservableList} of {@link Varbind}s returned by requests.
     * @return the {@link ObservableList} of {@link Varbind}s.
     */
    ObservableList<Varbind> getVarbinds() {
        return varbindsList;
    }

    /**
     * Getter for the amount of pending requests.
     * @return an {@link AtomicInteger} containing the amount of pending requests.
     */
    AtomicInteger getPendingRequests() {
        return pendingRequests;
    }
}
