package com.mitmax.backend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Settings {
    public static ObservableList<String> communities = FXCollections.observableArrayList("public", "private");
    public static ObservableList<String> initialRequests = FXCollections.observableArrayList(
            "1.3.6.1.2.1.1.5",  //sysName
            "1.3.6.1.2.1.1.1", //sysDescr
            "1.3.6.1.2.1.1.3", //sysUpTime
            "1.3.6.1.2.1.1.6", //sysLocation
            "1.3.6.1.2.1.1.4", //sysContact
            "1.3.6.1.2.1.1.7"); //sysServices
    public static ObservableList<String> mibModules = FXCollections.observableArrayList("SNMPv2-MIB", "IF-MIB");
}
