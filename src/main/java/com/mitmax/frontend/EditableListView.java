package com.mitmax.frontend;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class EditableListView extends BorderPane {
    private final ListView<String> listView;
    private ObservableList<String> backingList;
    private Label lbl_title;
    private int minItems;
    private int index;
    private String newText;
    private String titleText;

    public EditableListView() {
        index = 0;
        newText = "";
        titleText = "";
        minItems = 0;

        //Top
        lbl_title = new Label(titleText);
        this.setTop(lbl_title);

        //Center
        listView = new ListView<>();
        listView.setEditable(true);
        listView.setCellFactory(TextFieldListCell.forListView());
        listView.setPrefHeight(200);
        this.setCenter(listView);

        //Bottom
        HBox bottomItems = new HBox();
        Button btn_add = new Button("+");
        Button btn_remove = new Button("-");
        btn_add.setPrefWidth(Integer.MAX_VALUE);
        btn_add.setOnMouseClicked(e -> {
            backingList.add(newText + " " + index++);
            if(backingList.size() > minItems) {
                btn_remove.setDisable(false);
            }
        });
        btn_remove.setPrefWidth(Integer.MAX_VALUE);
        btn_remove.setOnMouseClicked(e -> {
            if(!listView.getSelectionModel().isEmpty()) {
                backingList.remove(listView.getSelectionModel().getSelectedItem());
                if(backingList.size() <= minItems) {
                    btn_remove.setDisable(true);
                }
            }
        });
        bottomItems.getChildren().addAll(btn_add, btn_remove);
        this.setBottom(bottomItems);
    }

    public ObservableList<String> getBackingList() {
        return backingList;
    }

    public void setBackingList(ObservableList<String> backingList) {
        this.backingList = backingList;
        listView.setItems(backingList);
    }

    public String getNewText() {
        return newText;
    }

    public void setNewText(String newText) {
        this.newText = newText;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
        lbl_title.setText(titleText);
    }

    public int getMinItems() {
        return minItems;
    }

    public void setMinItems(int minItems) {
        this.minItems = minItems;
    }
}
