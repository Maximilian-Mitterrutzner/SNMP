package com.mitmax.frontend;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Wrapper for an editable {@link ListView} which has a title
 * and supports adding and removing elements by buttons at the bottom.
 */
public class EditableListView extends BorderPane {
    private final ListView<String> listView;
    private ObservableList<String> backingList;
    private Label lbl_title;
    private int minItems;
    private int index;
    private String newText;
    private String titleText;

    /**
     * Constructs a new instance of the class.
     */
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

    /**
     * Getter for the backing {@link ObservableList} containing the items displayed in the {@link EditableListView}.
     * @return the backing {@link ObservableList}.
     */
    public ObservableList<String> getBackingList() {
        return backingList;
    }

    /**
     * Setter for the backing {@link ObservableList} containing the items displayed in the {@link EditableListView}.
     * @param backingList the new backing {@link ObservableList}.
     */
    public void setBackingList(ObservableList<String> backingList) {
        this.backingList = backingList;
        listView.setItems(backingList);
    }

    /**
     * Getter for the text displayed together with a incrementing index when a new element is added to the list.
     * @return a {@code String} containing the new-element-text.
     */
    public String getNewText() {
        return newText;
    }

    /**
     * Setter for the text displayed together with a incrementing index when a new element is added to the list.
     * @param newText a {@code String} containing the new new-element-text.
     */
    public void setNewText(String newText) {
        this.newText = newText;
    }

    /**
     * Getter for the text displayed above the {@link EditableListView}.
     * @return a {@code String} containing the title text.
     */
    public String getTitleText() {
        return titleText;
    }

    /**
     * Setter for the text displayed above the {@link EditableListView}.
     * @param titleText a {@code String} containing the new title text.
     */
    public void setTitleText(String titleText) {
        this.titleText = titleText;
        lbl_title.setText(titleText);
    }

    /**
     * Getter for the minimum amount of items that need to be in the {@link EditableListView} at any time.
     * @return a {@code int} specifying how many items need to be in the {@link EditableListView} at any time.
     */
    public int getMinItems() {
        return minItems;
    }

    /**
     * Setter for the minimum amount of items that need to be in the {@link EditableListView} at any time.
     * @param minItems a {@code int} specifying how many items now need to be in the
     * {@link EditableListView} at any time.
     */
    public void setMinItems(int minItems) {
        this.minItems = minItems;
    }
}
