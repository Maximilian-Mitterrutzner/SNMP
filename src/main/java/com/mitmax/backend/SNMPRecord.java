package com.mitmax.backend;

import com.mitmax.frontend.Controller;
import com.mitmax.frontend.LogLevel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.soulwing.snmp.*;

import java.util.List;

class SNMPRecord {
    private final SnmpContext context;
    private final ObservableList<Varbind> varbinds;
    private final String ip;
    private final String community;
    private final SNMPTarget parent;

    SNMPRecord(String ip, String community, SNMPTarget parent) {
        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress(ip);
        target.setCommunity(community);
        SimpleSnmpTargetConfig config = new SimpleSnmpTargetConfig();
        config.setTimeout(Controller.getTimeout() / 2);
        config.setRetries(1);
        context = SnmpFactory.getInstance().newContext(target, SNMPManager.getMib(), config, null);

        varbinds = FXCollections.observableArrayList();
        this.ip = ip;
        this.community = community;
        this.parent = parent;
    }

    void retrieve(List<String> oids, boolean isSubnet) {
        if(!isSubnet && Controller.getLogLevel() != LogLevel.NONE) {
            Controller.getLogger().logImmediately("Sent request! (" + ip + " - " + community + " - " + oids + ")");
        }

        context.asyncGetNext(event -> {
            try {
                VarbindCollection received = event.getResponse().get();
                Platform.runLater(() -> {
                    for(Varbind varbind : received) {
                        if(varbind.getOid().equals("1.3.6.1.2.1.1.5.0") || varbind.getName().equals("sysName.0")) {
                            parent.setHostName(varbind.asString() + " (" + ip + ")");
                            Controller.refreshListView();
                            break;
                        }
                    }
                    varbinds.addAll(received.asList());
                    parent.addSelfToList();
                });
            } catch (TimeoutException ex) {
                if(Controller.getLogLevel() == LogLevel.FULL
                || (Controller.getLogLevel() == LogLevel.SOME && !isSubnet)) {
                    Controller.getLogger().logBuffered("Request timed out! (" + ip + " - " + community + " - " + oids.toString() + ")");
                }
            } catch (SnmpException ex) {
                if(Controller.getLogLevel() != LogLevel.NONE) {
                    Controller.getLogger().logImmediately("An SNMP-Exception occurred, please restart the application");
                }
            } catch (Exception ex) {
                System.out.println("Unexpected Exception: ");
                ex.printStackTrace();
            }
        }, oids);
    }

    void close() {
        context.close();
    }

    ObservableList<Varbind> getVarbinds() {
        return varbinds;
    }
}
