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
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import java.util.Objects;

public class DndTabPane extends StackPane {

    private static final DataFormat tabMove = new DataFormat("DnDTabPane:tabMove");
    private static final Polyline marker = new Polyline();
    private final TabPane tabPane = new TabPane();

    public DndTabPane() {
        getChildren().addAll(tabPane, marker);
        tabPane.focusedProperty().addListener(this::handleFocused);
        marker.setStroke(Color.DARKORANGE);
        marker.setManaged(false);
        setOnDragOver(this::handleDragOver);
        setOnDragDone(this::handleDragDone);
    }

    public void add(EditorPane editorPane) {

        var label = new Label(editorPane.fileNameProperty().get());
        label.setOnDragDetected(this::handleDragDetected);
        var tab = new Tab();
        tab.setContent(editorPane);
        tab.setGraphic(label);
        tab.setOnClosed(e -> { if (tabPane.getTabs().isEmpty()) add(new EditorPane()); });
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        editorPane.fileNameProperty().addListener((ob, o, n) -> label.setText(n));

    }

    private void handleFocused(ObservableValue<? extends Boolean> ob, Boolean o, Boolean focused) {
        if (focused && !tabPane.getTabs().isEmpty()) {
            ((EditorPane) tabPane.getSelectionModel().getSelectedItem().getContent()).focus();
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
        Dragboard db = e.getDragboard();
        Object content = db.getContent(tabMove);
        if (Objects.nonNull(content) && content instanceof String cont) {
            Bounds bounds = tabPane.getLayoutBounds();
            if (isRightIn(e)) {
                marker.getPoints().addAll(
                        bounds.getCenterX(), 0.0,
                        bounds.getMaxX(), 0.0,
                        bounds.getMaxX(), bounds.getMaxY(),
                        bounds.getCenterX(), bounds.getMaxY()
                );
            } else if (isLeftIn(e)) {
                marker.getPoints().addAll(
                        0.0, 0.0,
                        bounds.getCenterX(), 0.0,
                        bounds.getCenterX(), bounds.getMaxY(),
                        0.0, bounds.getMaxY()
                );
            } else if (isBottomIn(e)) {
                marker.getPoints().addAll(
                        0.0, bounds.getCenterY(),
                        bounds.getMaxX(), bounds.getCenterY(),
                        bounds.getMaxX(), bounds.getMaxY(),
                        bounds.getMinX(), bounds.getMaxY()
                );
            } else if (isTopIn(e)) {
                marker.getPoints().addAll(
                        0.0, 0.0,
                        bounds.getMaxX(), 0.0,
                        bounds.getMaxX(), bounds.getCenterY(),
                        0.0, bounds.getCenterY()
                );
            } else {
                marker.getPoints().clear();
            }
        }
    }


    private void handleDragDone(DragEvent e) {
        marker.getPoints().clear();
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
        if (tabPane.getTabs().isEmpty()) return null;
        return tabPane.getTabs().getFirst().getGraphic().getParent().getParent();
    }

    private boolean isRightIn(DragEvent e) {
        Bounds paneBounds = localToScreen(getBoundsInLocal());
        double w = paneBounds.getWidth() / 4;
        double h = paneBounds.getHeight() / 4;
        return new BoundingBox(
                paneBounds.getMaxX() - w,
                paneBounds.getMinY() + h,
                w,
                paneBounds.getHeight() - h * 2
        ).contains(e.getScreenX(), e.getScreenY());
    }

    private boolean isLeftIn(DragEvent e) {
        Bounds paneBounds = localToScreen(getBoundsInLocal());
        double w = paneBounds.getWidth() / 4;
        double h = paneBounds.getHeight() / 4;
        return new BoundingBox(
                paneBounds.getMinX(),
                paneBounds.getMinY() + h,
                w,
                paneBounds.getHeight() - h * 2
        ).contains(e.getScreenX(), e.getScreenY());
    }

    private boolean isBottomIn(DragEvent e) {
        Bounds paneBounds = localToScreen(getBoundsInLocal());
        double w = paneBounds.getWidth() / 4;
        double h = paneBounds.getHeight() / 4;
        return new BoundingBox(
                paneBounds.getMinX() + w,
                paneBounds.getMaxY() - h,
                paneBounds.getWidth() - w * 2,
                h
        ).contains(e.getScreenX(), e.getScreenY());
    }

    private boolean isTopIn(DragEvent e) {
        Bounds paneBounds = localToScreen(getBoundsInLocal());
        double w = paneBounds.getWidth() / 4;
        double h = paneBounds.getHeight() / 4;
        return new BoundingBox(
                paneBounds.getMinX() + w,
                paneBounds.getMinY(),
                paneBounds.getWidth() - w * 2,
                h
        ).contains(e.getScreenX(), e.getScreenY());
    }

}
