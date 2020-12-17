package com.mitmax.backend;

import com.mitmax.frontend.Controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.soulwing.snmp.*;

import java.util.Arrays;

class SNMPRecord {
    private SnmpContext context;
    private ObservableList<Varbind> varbinds;
    private final String ip;
    private final String community;
    private final SNMPTarget parent;
    private int pendingRequests;

    SNMPRecord(String ip, String community, SNMPTarget parent) {
        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress(ip);
        target.setCommunity(community);
        SimpleSnmpTargetConfig config = new SimpleSnmpTargetConfig();
        config.setTimeout(2000);
        config.setRetries(1);
        context = SnmpFactory.getInstance().newContext(target, SNMPManager.getMib(), config, null);

        varbinds = FXCollections.observableArrayList();
        this.ip = ip;
        this.community = community;
        this.parent = parent;
    }

    void retrieve(String... oid) {
        pendingRequests++;
        context.asyncGetNext(event -> {
            try {
                VarbindCollection received = event.getResponse().get();
                Platform.runLater(() -> {
                    varbinds.addAll(received.asList());
                    Controller.refreshListView();
                });
            } catch (TimeoutException ex) {
                Controller.log("Request timed out! (" + ip + " - " + community + " - " + Arrays.toString(oid) + ")");
                parent.removeIfEmpty();
            } catch (SnmpException ex) {
                Controller.log("An SNMP-Exception occurred, please restart the application");
            } catch (Exception ex) {
                System.out.println("Unexpected Exception: ");
                ex.printStackTrace();
            }
            pendingRequests--;
        }, oid);
    }

    void close() {
        context.close();
    }

    ObservableList<Varbind> getVarbinds() {
        return varbinds;
    }

    int getPendingRequests() {
        return pendingRequests;
    }
}
