package com.mitmax;

import com.mitmax.backend.SNMPManager;
import com.mitmax.backend.Settings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Settings.load("./settings.txt");

        Parent root = FXMLLoader.load(getClass().getResource("/layout.fxml"));
        primaryStage.setTitle("SNMP");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        SNMPManager.closeAll();
        Settings.save("./settings.txt");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
