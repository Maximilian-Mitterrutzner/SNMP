package com.mitmax.frontend;

import com.mitmax.backend.SNMPManager;
import com.mitmax.backend.SNMPRecord;
import com.mitmax.backend.Settings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.soulwing.snmp.Varbind;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Controller {
    public ComboBox<String> cbx_mode;
    public TextField txt_ip;
    public ComboBox<String> cbx_scanCommunity;
    public ComboBox<String> cbx_requestCommunity;
    public HBox hbx_subnetItems;
    public Button btn_scan;
    public TextField txt_oid;
    public ListView<SNMPRecord> lsv_records;
    public TableView<Varbind> tbv_varbinds;

    @FXML
    public void initialize() {
        cbx_mode.setItems(FXCollections.observableArrayList("Host", "Subnetz"));
        cbx_mode.getSelectionModel().select(1);
        cbx_mode.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            boolean isShown = newValue.intValue() == 1;
            hbx_subnetItems.setVisible(isShown);
            hbx_subnetItems.setManaged(isShown);
        });

        cbx_scanCommunity.setItems(Settings.communities);
        cbx_scanCommunity.getSelectionModel().select(0);

        cbx_requestCommunity.setItems(Settings.communities);
        cbx_requestCommunity.getSelectionModel().select(0);

        btn_scan.setOnAction(this::onBtn_scan);

        lsv_records.setCellFactory(listView -> new ListCell<SNMPRecord>() {
            @Override
            protected void updateItem(SNMPRecord item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getIp());
                }
            }
        });
        lsv_records.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                tbv_varbinds.setItems(newValue.getVarbinds()));

        tbv_varbinds.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addColumn("Name", Varbind::getName);
        addColumn("Value", Varbind::toString);

        HBox.setHgrow(lsv_records, Priority.ALWAYS);
    }

    private void onBtn_scan(ActionEvent event) {
        String address = txt_ip.getText();
        try {
            InetAddress.getByName(address);
        } catch (UnknownHostException ex) {
            //TODO set text color to red
            return;
        }

        SNMPManager.scanAddress(address, cbx_scanCommunity.getSelectionModel().getSelectedItem());
    }

    private void addColumn(String name, Callback<Varbind, String> callback) {
        TableColumn<Varbind, String> column = new TableColumn<>(name);
        column.setCellValueFactory(param -> new SimpleStringProperty(callback.call(param.getValue())));
        tbv_varbinds.getColumns().add(column);
    }
}
