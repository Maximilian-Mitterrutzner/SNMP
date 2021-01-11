package com.mitmax;

import com.mitmax.backend.SNMPManager;
import com.mitmax.backend.Settings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class.
 */
public class Main extends Application {
    /**
     * Overridden function which is executed on program start.
     * It first reads settings from the disk then it loads the FXML.
     * @param primaryStage primary {@link Stage} supplied by JavaFX on startup.
     * @throws IOException the Exception thrown on unsuccessful FXML-load.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        Settings.load("./settings.txt");

        Parent root = FXMLLoader.load(getClass().getResource("/layout.fxml"));
        primaryStage.setTitle("SNMP");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Overridden function which is executed on program exit.
     * It saves the current settings to the disk.
     */
    @Override
    public void stop() {
        SNMPManager.closeAll();
        SNMPManager.closeListener();
        Settings.save("./settings.txt");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
