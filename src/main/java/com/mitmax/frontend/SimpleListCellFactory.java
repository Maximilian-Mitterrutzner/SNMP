package com.mitmax.frontend;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Simple factory class to generate simple {@link ListCell}s where just the text has to be set.
 * The text is supplied trough a {@link Callback} passed to the constructor.
 * @param <T> the type of the items in the {@link ListView}.
 */
public class SimpleListCellFactory<T> implements Callback<ListView<T>, ListCell<T>> {
    final Callback<T, String> nameCallable;

    /**
     * Creates a new instance of this class.
     * @param nameCallable the {@link Callback} used to supply the text for the {@link ListCell}.
     */
    SimpleListCellFactory(Callback<T, String> nameCallable) {
        this.nameCallable = nameCallable;
    }

    /**
     * The function called by the {@link ListView} to generate {@link ListCell}s.
     * @param param the {@link ListView} in which this {@link SimpleListCellFactory} is used as a {@code cellFactory}.
     * @return a new {@link ListCell} with the specified text.
     */
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
