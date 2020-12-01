package com.mitmax;

import com.mitmax.backend.SNMPManager;
import com.mitmax.frontend.WindowManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SNMP");
        WindowManager.initialize(primaryStage);
        WindowManager.start();
    }

    @Override
    public void stop() {
        SNMPManager.closeAll();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
