package com.mitmax;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        new SimpleIntegerProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("test");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
