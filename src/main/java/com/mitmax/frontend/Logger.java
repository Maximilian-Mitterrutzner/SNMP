package com.mitmax.frontend;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Logger {
    private final TextArea outputArea;
    private final ArrayList<String> messageBuffer;

    Logger(TextArea outputArea) {
        this.outputArea = outputArea;
        messageBuffer = new ArrayList<>();

        Timer outputTimer = new Timer(true);
        outputTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onOutput();
            }
        }, 0, 2000);
    }

    synchronized void log(String message) {
        messageBuffer.add(message);
    }

    private synchronized void onOutput() {
        int messageCount = messageBuffer.size();
        if(messageCount == 0) {
            return;
        }

        StringBuilder outputBuilder = new StringBuilder(messageCount);

        for(String message : messageBuffer) {
            outputBuilder.append(message).append("\n");
        }

        String toAppend = outputBuilder.toString();
        Platform.runLater(() -> outputArea.appendText(toAppend));

        messageBuffer.clear();
    }
}
