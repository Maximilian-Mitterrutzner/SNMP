package com.mitmax.frontend;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ModeTabPane<T extends Node> extends TabPane {
    private T content;

    void setContent(T content) {
        this.content = content;
        Tab selected = this.getSelectionModel().getSelectedItem();
        if(selected != null) {
            selected.setContent(content);
        }
    }

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
