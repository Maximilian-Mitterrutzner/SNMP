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
                flush();
            }
        }, 0, 2000);
    }

    public synchronized void logBuffered(String message) {
        messageBuffer.add(message);
    }

    public void logImmediately(String message) {
        if(Platform.isFxApplicationThread()) {
            outputArea.appendText(message + "\n");
        }
        else {
            Platform.runLater(() -> outputArea.appendText(message + "\n"));
        }
    }

    public synchronized void flush() {
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
