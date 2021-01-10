package com.mitmax.backend;

import com.mitmax.frontend.LogLevel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;

public class Settings {
    public static LogLevel logLevel = LogLevel.SOME;
    public static int timeout = 5000;
    public static ObservableList<String> communities = FXCollections.observableArrayList("public", "private");
    public static ObservableList<String> initialRequests = FXCollections.observableArrayList(
            "1.3.6.1.2.1.1.5",  //sysName
            "1.3.6.1.2.1.1.1", //sysDescr
            "1.3.6.1.2.1.1.3", //sysUpTime
            "1.3.6.1.2.1.1.6", //sysLocation
            "1.3.6.1.2.1.1.4", //sysContact
            "1.3.6.1.2.1.1.7"); //sysServices
    public static ObservableList<String> mibModules = FXCollections.observableArrayList("SNMPv2-MIB", "IF-MIB");

    public static void save(String path) {
        try {
            File file = new File(path);

            if(!file.exists()) {
                if(!file.createNewFile()) {
                    return;
                }
            }

            FileWriter writer = new FileWriter(file);
            writer.write(logLevel.toString() + "\n");
            writer.write(timeout + "\n");
            writer.write(listToString(communities) + "\n");
            writer.write(listToString(initialRequests) + "\n");
            writer.write(listToString(mibModules) + "\n");
            writer.flush();
            writer.close();
        } catch (IOException ignore) {}
    }

    public static void load(String path) {
        try {
            File file = new File(path);

            if(!file.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            logLevel = Enum.valueOf(LogLevel.class, reader.readLine());
            timeout = Integer.parseInt(reader.readLine());
            communities.setAll(reader.readLine().split("#"));
            initialRequests.setAll(reader.readLine().split("#"));
            mibModules.setAll(reader.readLine().split("#"));
        } catch (Exception ex) {
            logLevel = LogLevel.SOME;
            timeout = 5000;
            communities = FXCollections.observableArrayList("public", "private");
            initialRequests = FXCollections.observableArrayList(
                    "1.3.6.1.2.1.1.5",
                    "1.3.6.1.2.1.1.1",
                    "1.3.6.1.2.1.1.3",
                    "1.3.6.1.2.1.1.6",
                    "1.3.6.1.2.1.1.4",
                    "1.3.6.1.2.1.1.7");
            mibModules = FXCollections.observableArrayList("SNMPv2-MIB", "IF-MIB");
        }
    }

    private static String listToString(ObservableList<String> list) {
        StringBuilder builder = new StringBuilder();
        for(String request : list) {
            builder.append(request).append("#");
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }
}
