package com.mitmax.frontend;

import com.mitmax.backend.SNMPManager;
import com.mitmax.backend.SNMPRecord;
import com.mitmax.backend.Settings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.soulwing.snmp.Varbind;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WindowManager {
    static private Stage primaryStage;

    public static void initialize(Stage _primaryStage) {
        primaryStage = _primaryStage;
    }

    public static void start() {
        VBox root = new VBox(10);

        //Scan
        HBox hbx_topItems = new HBox(10);
        ComboBox<String> cbx_mode = new ComboBox<>(FXCollections.observableArrayList("Host", "Subnetz"));
        cbx_mode.getSelectionModel().select(0);
        TextField txt_ip = new TextField();
        txt_ip.setPromptText("192.168.1.1");

        HBox hbx_subnetItems = new HBox(10);
        TextField txt_mask = new TextField();
        txt_mask.setPromptText("24");
        Label lbl_slash = new Label("/");
        lbl_slash.setPadding(new Insets(-5, 0, 0, 0));
        lbl_slash.setFont(new Font(20));
        hbx_subnetItems.getChildren().addAll(lbl_slash, txt_mask);
        hbx_subnetItems.setVisible(false);
        hbx_subnetItems.setManaged(false);
        cbx_mode.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            boolean isShown = newValue.intValue() == 0;
            hbx_subnetItems.setVisible(isShown);
            hbx_subnetItems.setManaged(isShown);
        });

        ComboBox<String> cbx_scanCommunity = new ComboBox<>(Settings.communities);
        cbx_scanCommunity.getSelectionModel().select(0);

        Button btn_scan = new Button("Scan");
        btn_scan.setOnAction(event -> {
            String address = txt_ip.getText();
            try {
                InetAddress.getByName(address);
            } catch (UnknownHostException ex) {
                //TODO set text color to red
                return;
            }

            SNMPManager.scanAddress(address, cbx_scanCommunity.getSelectionModel().getSelectedItem());
        });

        hbx_topItems.getChildren().addAll(cbx_mode, txt_ip, hbx_subnetItems, cbx_scanCommunity, btn_scan);

        //Display
        HBox hbx_displayItems = new HBox(10);
        ListView<SNMPRecord> lsv_records = new ListView<>(SNMPManager.snmpRecords);
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
        TableView<Varbind> tbv_varbinds = new TableView<>();
        TableColumn<Varbind, String> clm_name = new TableColumn<>("Name");
        clm_name.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getName()));
        TableColumn<Varbind, String> clm_value = new TableColumn<>("Value");
        clm_value.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().toString()));
        tbv_varbinds.getColumns().addAll(clm_name, clm_value);
        tbv_varbinds.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        lsv_records.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                tbv_varbinds.setItems(newValue.getVarbinds()));
        hbx_displayItems.getChildren().addAll(lsv_records, tbv_varbinds);

        //Manual retrieve
        GridPane grp_retrieveItems = new GridPane();

        grp_retrieveItems.add(new Label("OID"), 0, 0);
        TextField txt_oid = new TextField();
        txt_oid.setPromptText("1.3.6.1.2.1.1.3.0");
        grp_retrieveItems.add(txt_oid, 0, 1);

        grp_retrieveItems.add(new Label("Community"), 1, 0);
        ComboBox<String> cbx_retrieveCommunity = new ComboBox<>(Settings.communities);
        cbx_retrieveCommunity.getSelectionModel().select(0);
        grp_retrieveItems.add(cbx_retrieveCommunity, 1, 1);

        Button btn_retrieve = new Button("Retrieve");
        grp_retrieveItems.add(btn_retrieve, 2, 1);

        grp_retrieveItems.setHgap(10);
        grp_retrieveItems.setVgap(2);

        //Formatting
        root.getChildren().addAll(hbx_topItems, hbx_displayItems, grp_retrieveItems);
        root.setPadding(new Insets(10));

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
