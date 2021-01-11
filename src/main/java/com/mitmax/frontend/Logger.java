package com.mitmax.frontend;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Helper class to manage access to a particular {@link TextArea} with the ability to buffer logs
 * to increase performance and responsibility.
 */
public class Logger {
    private final TextArea outputArea;
    private final ArrayList<String> messageBuffer;

    /**
     * Creates a new instance of the class.
     * Creates a timer which periodically calls the {@code flush()} method.
     * @param outputArea the {@link TextArea} to manage access to.
     */
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

    /**
     * Add a message to the buffer, which is emptied periodically.
     * @param message the {@code String} containing the message to buffer.
     */
    public synchronized void logBuffered(String message) {
        messageBuffer.add(message);
    }

    /**
     * Logs a message immediately, bypassing the buffer.
     * @param message the {@code String} containing the message to log immediately.
     */
    public void logImmediately(String message) {
        if(Platform.isFxApplicationThread()) {
            outputArea.appendText(message + "\n");
        }
        else {
            Platform.runLater(() -> outputArea.appendText(message + "\n"));
        }
    }

    /**
     * Flushed the buffer.
     * This function concatenates all the messages to a single {@code String} using a {@link StringBuilder}.
     * The concatenated messages are then passed to the method <br> {@link TextArea}{@code #append(String)} in
     * a single {@link Platform}{@code #runLater(}{@link Runnable}{@code )}.
     */
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
