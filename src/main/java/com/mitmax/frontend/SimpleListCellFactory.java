package com.mitmax.frontend;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class SimpleListCellFactory<T> implements Callback<ListView<T>, ListCell<T>> {
    final Callback<T, String> nameCallable;

    SimpleListCellFactory(Callback<T, String> nameCallable) {
        this.nameCallable = nameCallable;
    }

    @Override
    public ListCell<T> call(ListView<T> param) {
        return new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(nameCallable.call(item));
                }
            }
        };
    }
}
