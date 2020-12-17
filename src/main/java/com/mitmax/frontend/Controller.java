package com.mitmax.frontend;

import com.mitmax.backend.SNMPManager;
import com.mitmax.backend.SNMPTarget;
import com.mitmax.backend.Settings;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.soulwing.snmp.Varbind;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Controller {
    public ComboBox<String> cbx_mode;
    public TextField txt_scanIp;
    public ComboBox<String> cbx_scanCommunity;
    public ComboBox<String> cbx_requestCommunity;
    public HBox hbx_subnetItems;
    public Button btn_scan;
    public TextField txt_oid;
    public ListView<SNMPTarget> lsv_records;
    public TextField txt_requestIp;
    public TabPane tbp_communities;
    public TextArea txa_log;
    public EditableListView lsv_communities;
    public EditableListView lsv_initialRequests;
    public EditableListView lsv_mibModules;

    private static TextArea staticTxa_log;
    private static ListView<SNMPTarget> staticLsv_records;

    private TableView<Varbind> tbv_varbinds;

    @FXML
    public void initialize() {
        cbx_mode.setItems(FXCollections.observableArrayList("Host", "Subnetz"));
        cbx_mode.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            boolean isShown = newValue.intValue() == 1;
            hbx_subnetItems.setVisible(isShown);
            hbx_subnetItems.setManaged(isShown);
        });
        cbx_mode.getSelectionModel().select(0);

        cbx_scanCommunity.setItems(Settings.communities);
        cbx_scanCommunity.getSelectionModel().select(0);

        cbx_requestCommunity.setItems(Settings.communities);
        cbx_requestCommunity.getSelectionModel().select(0);

        btn_scan.setOnAction(this::onBtn_scan);

        lsv_records.setCellFactory(listView -> new ListCell<SNMPTarget>() {
            @Override
            protected void updateItem(SNMPTarget item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    for(String community : Settings.communities) {
                        for(Varbind varbind : item.getVarbinds(community)) {
                            if(varbind.getOid().equals("1.3.6.1.2.1.1.5.0") || varbind.getName().equals("sysName.0")) {
                                setText(varbind.asString() + " (" + item.getIp() + ")");
                                return;
                            }
                        }
                    }
                    setText(item.getIp());
                }
            }
        });
        lsv_records.getSelectionModel().selectedItemProperty().addListener((observable, oldRecord, newRecord) -> {
            if(!tbp_communities.getSelectionModel().isEmpty()) {
                onSelectionChanged(tbp_communities.getSelectionModel().getSelectedItem(), newRecord);
            }
        });
        lsv_records.setItems(SNMPManager.getSnmpTargets());
        tbv_varbinds = new TableView<>();
        addColumn("OID", 0.2, Varbind::getOid);
        addColumn("Name", 0.2, Varbind::getName);
        addColumn("Value", 0.6, Varbind::asString);

        tbp_communities.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if(oldTab != null) {
                oldTab.setContent(null);
            }
            if(newTab != null) {
                newTab.setContent(tbv_varbinds);
                if(!lsv_records.getSelectionModel().isEmpty()) {
                    onSelectionChanged(newTab, lsv_records.getSelectionModel().getSelectedItem());
                }
            }
        });
        for(String community : Settings.communities) {
            tbp_communities.getTabs().add(new Tab(community));
        }

        lsv_records.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        tbp_communities.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        lsv_communities.setBackingList(Settings.communities);
        lsv_communities.setNewText("Community");
        lsv_communities.setTitleText("Communities");
        lsv_communities.setMinItems(1);
        lsv_initialRequests.setBackingList(Settings.initialRequests);
        lsv_initialRequests.setNewText("Request");
        lsv_initialRequests.setTitleText("Initial Requests");
        lsv_mibModules.setBackingList(Settings.mibModules);
        lsv_mibModules.setNewText("Module");
        lsv_mibModules.setTitleText("MIB Modules");

        staticTxa_log = txa_log;
        staticLsv_records = lsv_records;
    }

    private void onBtn_scan(ActionEvent event) {
        String address = txt_scanIp.getText();
        try {
            InetAddress.getByName(address);
        } catch (UnknownHostException ex) {
            //TODO set text color to red
            return;
        }

        SNMPManager.scanAddress(address, cbx_scanCommunity.getSelectionModel().getSelectedItem());
    }

    private void addColumn(String name, double size, Callback<Varbind, String> callback) {
        TableColumn<Varbind, String> column = new TableColumn<>(name);
        column.prefWidthProperty().bind(tbv_varbinds.widthProperty().multiply(size).subtract(1));
        column.setResizable(false);
        column.setCellValueFactory(param -> new SimpleStringProperty(callback.call(param.getValue())));
        tbv_varbinds.getColumns().add(column);
    }

    private void onSelectionChanged(Tab selectedTab, SNMPTarget selectedRecord) {
        tbv_varbinds.setItems(selectedRecord.getVarbinds(selectedTab.getText()));
    }

    public static void log(String message) {
        Platform.runLater(() -> staticTxa_log.appendText(message + "\n"));
    }

    public static void refreshListView() {
        staticLsv_records.refresh();
    }
}
