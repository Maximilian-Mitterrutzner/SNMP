package com.mitmax.backend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Settings {
    public static ObservableList<String> communities = FXCollections.observableArrayList("public", "private");
    public static ObservableList<String> initialRequests = FXCollections.observableArrayList("1.3.6.1.2.1.1.3.0");
}
