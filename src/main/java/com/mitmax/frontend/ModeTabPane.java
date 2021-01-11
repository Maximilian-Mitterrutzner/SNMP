package com.mitmax.frontend;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Wrapper class for a {@link TabPane} which only has a single child
 * where the actual {@link Tab}s are used as a kind of "mode" used in the child.
 * @param <T> The type of the single content which is supposed to be inside the {@link TabPane}.
 */
public class ModeTabPane<T extends Node> extends TabPane {
    private T content;

    /**
     * Setter for the content.
     * @param content the new content of type {@link T}.
     */
    void setContent(T content) {
        this.content = content;
        Tab selected = this.getSelectionModel().getSelectedItem();
        if(selected != null) {
            selected.setContent(content);
        }
    }

    /**
     * Adds a listener to listen for a change in what {@link Tab} is currently selected.
     * The listener is only fired if there is an actual {@link Tab} selected.
     * @param listener the {@link ChangeListener} to add to the selectedItemProperty of the {@link TabPane}.
     */
    void setOnTabSelected(ChangeListener<Tab> listener) {
        this.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if(oldTab != null) {
                oldTab.setContent(null);
            }
            if(newTab != null) {
                newTab.setContent(content);
                listener.changed(observable, oldTab, newTab);
            }
        });
    }
}
