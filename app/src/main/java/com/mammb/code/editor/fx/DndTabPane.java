/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor.fx;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import java.util.Objects;

public class DndTabPane extends TabPane {

    private static final DataFormat tabMove = new DataFormat("DnDTabPane:tabMove");

    public DndTabPane() {
        focusedProperty().addListener(this::handleFocused);
        setOnDragOver(this::handleDragOver);
    }

    public void add(EditorPane editorPane) {

        var label = new Label(editorPane.fileNameProperty().get());
        label.setOnDragDetected(this::handleDragDetected);
        var tab = new Tab();
        tab.setContent(editorPane);
        tab.setGraphic(label);
        tab.setOnClosed(e -> { if (getTabs().isEmpty()) add(new EditorPane()); });
        getTabs().add(tab);
        getSelectionModel().select(tab);
        editorPane.fileNameProperty().addListener((ob, o, n) -> label.setText(n));

    }

    private void handleFocused(ObservableValue<? extends Boolean> ob, Boolean o, Boolean focused) {
        if (focused && !getTabs().isEmpty()) {
            ((EditorPane) getSelectionModel().getSelectedItem().getContent()).focus();
        }
    }

    private void handleDragDetected(MouseEvent e) {
        if (e.getSource() instanceof Label label) {
            Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.put(tabMove, String.valueOf(System.identityHashCode(label)));
            Image image = tabImage(label);
            db.setDragView(image, image.getWidth(), -image.getHeight());
            db.setContent(cc);
        }
    }

    private void handleDragOver(DragEvent e) {
    }

    private void handleDragDone(DragEvent e) {
    }

    private Image tabImage(Node node) {
        for (;;) {
            node = node.getParent();
            if (Objects.equals(
                    node.getClass().getSimpleName(),
                    "TabHeaderSkin")) break;
        }
        var snapshotParams = new SnapshotParameters();
        snapshotParams.setFill(Color.TRANSPARENT);
        return node.snapshot(snapshotParams, null);
    }

    private Node tabHeader() {
        if (getTabs().isEmpty()) return null;
        return getTabs().getFirst().getGraphic().getParent().getParent();
    }

}
