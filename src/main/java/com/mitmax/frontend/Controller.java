package com.mitmax.frontend;

import com.mitmax.backend.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.soulwing.snmp.*;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {
    public TabPane tbp_mode;
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
    public ModeTabPane<TableView<Varbind>> mtp_communities;
    public TextArea txa_log;
    public EditableListView lsv_communities;
    public EditableListView lsv_initialRequests;
    public EditableListView lsv_mibModules;
    public TextField txt_mask;
    public TextField txt_timeout;
    public ToggleGroup tgg_logLevel;
    public ModeTabPane<ListView<SnmpNotification>> mtp_trapTypes;
    public BorderPane brp_container;

    private static ListView<SNMPTarget> staticLsv_records;
    private static TextField staticTxt_timeout;

    private TableView<Varbind> tbv_varbinds;
    private ListView<SnmpNotification> lsv_traps;
    private static Logger logger;
    private static LogLevel logLevel;
    private static ObservableList<SnmpNotification> trapsList;
    private static ObservableList<SnmpNotification> informsList;

    @FXML
    public void initialize() {
        //TabPane
        tbv_varbinds = new TableView<>();
        tbp_mode.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            tbv_varbinds.setItems(null);

            if(newTab.getText().equals("Scanner")) {
                brp_container.setCenter(null);
                mtp_communities.setContent(tbv_varbinds);
                lsv_records.getSelectionModel().clearSelection();
            }
            else {
                mtp_communities.setContent(null);
                brp_container.setCenter(tbv_varbinds);
                lsv_traps.getSelectionModel().clearSelection();
            }
        });

        //Scanner
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
        lsv_records.setCellFactory(new SimpleListCellFactory<>(SNMPTarget::getHostName));
        lsv_records.getSelectionModel().selectedItemProperty().addListener((observable, oldRecord, newRecord) -> {
            if(!mtp_communities.getSelectionModel().isEmpty()) {
                onSelectionChanged(mtp_communities.getSelectionModel().getSelectedItem(), newRecord);
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
        addColumn("OID", 0.2, Varbind::getOid);
        addColumn("Name", 0.2, Varbind::getName);
        addColumn("Value", 0.6, Varbind::asString);

        mtp_communities.setContent(tbv_varbinds);
        mtp_communities.setOnTabSelected((observable, oldTab, newTab) -> {
            if(!lsv_records.getSelectionModel().isEmpty()) {
                onSelectionChanged(newTab, lsv_records.getSelectionModel().getSelectedItem());
            }
        });
        for(String community : Settings.communities) {
            mtp_communities.getTabs().add(new Tab(community));
        }

        lsv_records.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        mtp_communities.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        txt_scanIp.textProperty().addListener((observable, oldValue, newValue) -> txt_scanIp.setStyle(null));
        txt_mask.textProperty().addListener((observable, oldValue, newValue) -> txt_mask.setStyle(null));
        txt_endIP.textProperty().addListener((observable, oldValue, newValue) -> txt_endIP.setStyle(null));
        txt_requestIp.textProperty().addListener((observable, oldValue, newValue) -> txt_requestIp.setStyle(null));
        txt_oid.textProperty().addListener((observable, oldValue, newValue) -> txt_oid.setStyle(null));

        btn_request.setOnAction(this::onBtn_request);

        //Traps
        lsv_traps = new ListView<>();
        trapsList = FXCollections.observableArrayList();
        informsList = FXCollections.observableArrayList();

        mtp_trapTypes.setContent(lsv_traps);
        mtp_trapTypes.setOnTabSelected((observable, oldTab, newTab) -> {
            lsv_traps.setItems(newTab.getText().equals("Traps") ? trapsList : informsList);
        });
        mtp_trapTypes.getTabs().add(new Tab("Traps"));
        mtp_trapTypes.getTabs().add(new Tab("Informs"));

        lsv_traps.setCellFactory(new SimpleListCellFactory<>(item -> item.getPeer().getAddress()));
        lsv_traps.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) {
                tbv_varbinds.setItems(null);
                return;
            }

            tbv_varbinds.setItems(FXCollections.observableArrayList(newValue.getVarbinds().asList()));
            tbv_varbinds.getSortOrder().add(tbv_varbinds.getColumns().get(0));
            tbv_varbinds.sort();
        });

        mtp_trapTypes.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        SNMPManager.registerTrapListener();

        //Settings
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
        long addressBinary;

        try {
            addressBinary = AddressHelper.getAsBinary(address);
        } catch (Exception ex) {
            txt_scanIp.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            return;
        }

        switch (cbx_mode.getSelectionModel().getSelectedItem()) {
            case "Host":
                SNMPManager.scanAddress(address, addressBinary, community, Settings.initialRequests, false);
                break;
            case "Subnet":
                int mask;
                try {
                    mask = Integer.parseInt(txt_mask.getText());
                    if(mask < 1 || mask > 32) {
                        throw new NumberFormatException();
                    }
                }
                catch (Exception ex) {
                    txt_mask.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                    return;
                }

                SNMPManager.scanSubnet(address, community, mask);
                break;
            case "Range":
                String endIp = txt_endIP.getText();
                long endAddress;
                try {
                    endAddress = AddressHelper.getAsBinary(endIp);

                    if(endAddress < addressBinary) {
                        throw new IllegalArgumentException();
                    }
                } catch (Exception ex) {
                    txt_endIP.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                    return;
                }

                SNMPManager.scanRange(addressBinary, endAddress, community);
                break;
        }
    }

    private void onBtn_request(ActionEvent event) {
        String community = cbx_requestCommunity.getSelectionModel().getSelectedItem();
        String oid = txt_oid.getText();
        String address = txt_requestIp.getText();
        long addressBinary;

        try {
            addressBinary = AddressHelper.getAsBinary(address);
        } catch (Exception ex) {
            txt_requestIp.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            return;
        }

        if(oid.trim().isEmpty()) {
            txt_oid.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            return;
        }

        SNMPManager.scanAddress(address, addressBinary, community, Collections.singletonList(oid), false);
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

        ObservableList<Varbind> varbinds = selectedRecord.getVarbinds(selectedTab.getText());
        AtomicBoolean isSorting = new AtomicBoolean(false);
        varbinds.addListener((ListChangeListener<Varbind>) c -> {
            if(!isSorting.get()) {
                isSorting.set(true);
                tbv_varbinds.sort();
                isSorting.set(false);
            }
        });
        tbv_varbinds.setItems(varbinds);
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

    public static void addNotification(SnmpNotification notification) {
        Platform.runLater(() -> {
            if(notification.getType().equals(SnmpNotification.Type.TRAP)) {
                trapsList.add(notification);
            }
            else {
                informsList.add(notification);
            }
        });
    }
}
