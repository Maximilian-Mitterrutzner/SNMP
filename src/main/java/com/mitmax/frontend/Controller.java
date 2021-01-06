package com.mitmax.frontend;

import com.mitmax.backend.AddressHelper;
import com.mitmax.backend.SNMPManager;
import com.mitmax.backend.SNMPTarget;
import com.mitmax.backend.Settings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.soulwing.snmp.Varbind;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

public class Controller {
    public ComboBox<String> cbx_mode;
    public TextField txt_scanIp;
    public ComboBox<String> cbx_scanCommunity;
    public ComboBox<String> cbx_requestCommunity;
    public HBox hbx_subnetItems;
    public HBox hbx_rangeItems;
    public TextField txt_endIP;
    public Button btn_scan;
    public TextField txt_oid;
    public ListView<SNMPTarget> lsv_records;
    public TextField txt_requestIp;
    public Button btn_request;
    public TabPane tbp_communities;
    public TextArea txa_log;
    public EditableListView lsv_communities;
    public EditableListView lsv_initialRequests;
    public EditableListView lsv_mibModules;
    public TextField txt_mask;
    public TextField txt_timeout;
    public ToggleGroup tgg_logLevel;

    private static ListView<SNMPTarget> staticLsv_records;
    private static TextField staticTxt_timeout;

    private TableView<Varbind> tbv_varbinds;
    private static Logger logger;
    private static LogLevel logLevel;

    @FXML
    public void initialize() {
        cbx_mode.setItems(FXCollections.observableArrayList("Host", "Subnet", "Range"));
        cbx_mode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            hbx_subnetItems.setVisible(newValue.equals("Subnet"));
            hbx_subnetItems.setManaged(newValue.equals("Subnet"));
            hbx_rangeItems.setVisible(newValue.equals("Range"));
            hbx_rangeItems.setManaged(newValue.equals("Range"));
        });
        cbx_mode.getSelectionModel().select(0);

        cbx_scanCommunity.setItems(Settings.communities);
        cbx_scanCommunity.getSelectionModel().select(0);

        cbx_requestCommunity.setItems(Settings.communities);
        cbx_requestCommunity.getSelectionModel().select(0);

        btn_scan.setOnAction(this::onBtn_scan);

        ObservableList<SNMPTarget> backingRecordsList = FXCollections.observableArrayList();
        lsv_records.setItems(new SortedList<>(backingRecordsList, SNMPTarget::compareTo));
        lsv_records.setCellFactory(listView -> new ListCell<SNMPTarget>() {
            @Override
            protected void updateItem(SNMPTarget item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getHostName());
                }
            }
        });
        lsv_records.getSelectionModel().selectedItemProperty().addListener((observable, oldRecord, newRecord) -> {
            if(!tbp_communities.getSelectionModel().isEmpty()) {
                onSelectionChanged(tbp_communities.getSelectionModel().getSelectedItem(), newRecord);
            }
        });
        SNMPManager.getSnmpTargets().addListener((MapChangeListener<String, SNMPTarget>) change -> {
            if(change.wasRemoved()) {
                backingRecordsList.remove(change.getValueRemoved());
            }
            if(change.wasAdded()) {
                backingRecordsList.add(change.getValueAdded());
            }
        });
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

        btn_request.setOnAction(this::onBtn_request);

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

        staticLsv_records = lsv_records;
        staticTxt_timeout = txt_timeout;

        logger = new Logger(txa_log);

        txt_timeout.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[1-9][0-9]{0,5}")) {
                txt_timeout.setText(oldValue);
            }
        });

        logLevel = LogLevel.SOME;
        tgg_logLevel.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            logLevel = LogLevel.values()[tgg_logLevel.getToggles().indexOf(newValue)];
            txa_log.setVisible(logLevel != LogLevel.NONE);
            txa_log.setManaged(logLevel != LogLevel.NONE);
        });
    }

    private void onBtn_scan(ActionEvent event) {
        String community = cbx_scanCommunity.getSelectionModel().getSelectedItem();
        String address = txt_scanIp.getText();
        if(!isAddressValid(address)) {
            //TODO set text color to red
            return;
        }

        switch (cbx_mode.getSelectionModel().getSelectedItem()) {
            case "Host":
                SNMPManager.scanAddress(address, community, Settings.initialRequests, false);
                break;
            case "Subnet":
                try {
                    int mask = Integer.parseInt(txt_mask.getText());
                    if(mask < 1 || mask > 32) {
                        throw new NumberFormatException();
                    }
                    SNMPManager.scanSubnet(address, community, mask);
                }
                catch (NumberFormatException ex) {
                    //TODO set text color to red
                }
                break;
            case "Range":
                String endIp = txt_endIP.getText();
                long startAddress = AddressHelper.getAsBinary(address);
                long endAddress = AddressHelper.getAsBinary(endIp);
                if(!isAddressValid(endIp)
                || endAddress < startAddress) {
                    //TODO set text color to red
                    return;
                }

                SNMPManager.scanRange(startAddress, endAddress, community);
                break;
        }
    }

    private void onBtn_request(ActionEvent event) {
        String community = cbx_requestCommunity.getSelectionModel().getSelectedItem();
        String oid = txt_oid.getText();
        String address = txt_requestIp.getText();

        if(!isAddressValid(address)) {
            //TODO set text color to red
            return;
        }

        SNMPManager.scanAddress(address, community, Collections.singletonList(oid), false);
    }

    private boolean isAddressValid(String address) {
        try {
            InetAddress.getByName(address);
            return true;
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    private void addColumn(String name, double size, Callback<Varbind, String> callback) {
        TableColumn<Varbind, String> column = new TableColumn<>(name);
        column.prefWidthProperty().bind(tbv_varbinds.widthProperty().multiply(size).subtract(1));
        column.setResizable(false);
        column.setCellValueFactory(param -> new SimpleStringProperty(callback.call(param.getValue())));
        tbv_varbinds.getColumns().add(column);
    }

    private void onSelectionChanged(Tab selectedTab, SNMPTarget selectedRecord) {
        if(selectedRecord == null) {
            tbv_varbinds.setItems(null);
            return;
        }
        tbv_varbinds.setItems(selectedRecord.getVarbinds(selectedTab.getText()));
        tbv_varbinds.getSortOrder().add(tbv_varbinds.getColumns().get(0));
        tbv_varbinds.sort();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static LogLevel getLogLevel() {
        return logLevel;
    }

    public static void refreshListView() {
        staticLsv_records.refresh();
    }

    public static int getTimeout() {
        return Integer.parseInt(staticTxt_timeout.getText());
    }
}
