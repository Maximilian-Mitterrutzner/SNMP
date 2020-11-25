package com.mitmax;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.soulwing.snmp.*;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        BorderPane root = new BorderPane();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress("10.10.30.254");
        target.setCommunity("public");

        Mib mib = MibFactory.getInstance().newMib();
        mib.load("SNMPv2-MIB");
        mib.load("IF-MIB");

        try (SnmpContext context = SnmpFactory.getInstance().newContext(target)) {
            VarbindCollection varbinds = context.get("1.3.6.1.2.1.1.3.0").get();
            System.out.println(varbinds.get(0).getName() + ": " + varbinds.get(0).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
