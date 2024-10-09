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
import javafx.event.Event;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
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
import javafx.scene.shape.Rectangle;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class SplitTabPane extends StackPane {

    private static final AtomicReference<Tab> draggedTab = new AtomicReference<>();
    private static final AtomicReference<DndTabPane> activePane = new AtomicReference<>();

    private static final DataFormat tabMove = new DataFormat("SplitTabPane:tabMove");

    private SplitPane pane = new SplitPane();

    public SplitTabPane() {
        getChildren().add(pane);
    }
    public SplitTabPane(EditorPane node) {
        this();
        add(node);
    }
    private SplitTabPane(DndTabPane node) {
        this();
        pane.getItems().add(node.parentWith(this));
    }
    public void add(EditorPane node) {
        pane.getItems().clear();
        pane.getItems().add(new DndTabPane(this, node));
    }
    public void addRight(EditorPane node) {
        if (pane.getItems().isEmpty()) {
            add(node);
        } else {
            var item = (DndTabPane) pane.getItems().getFirst();
            pane.getItems().clear();
            pane.setOrientation(Orientation.HORIZONTAL);
            pane.getItems().addAll(new SplitTabPane(item), new SplitTabPane(node));
        }
    }
    public void addLeft(EditorPane node) {
        if (pane.getItems().isEmpty()) {
            add(node);
        } else {
            var item = (EditorPane) pane.getItems().getFirst();
            pane.getItems().clear();
            pane.setOrientation(Orientation.HORIZONTAL);
            pane.getItems().addAll(new SplitTabPane(node), new SplitTabPane(item));
        }
    }
    public void addTop(EditorPane node) {
        if (pane.getItems().isEmpty()) {
            add(node);
        } else {
            var item = (EditorPane) pane.getItems().getFirst();
            pane.getItems().clear();
            pane.setOrientation(Orientation.VERTICAL);
            pane.getItems().addAll(new SplitTabPane(node), new SplitTabPane(item));
        }
    }
    public void addBottom(EditorPane node) {
        if (pane.getItems().isEmpty()) {
            add(node);
        } else {
            var item = (EditorPane) pane.getItems().getFirst();
            pane.getItems().clear();
            pane.setOrientation(Orientation.VERTICAL);
            pane.getItems().addAll(new SplitTabPane(item), new SplitTabPane(node));
        }
    }

    static class DndTabPane extends StackPane {
        private final TabPane tabPane = new TabPane();
        private final Rectangle marker = new Rectangle();
        private SplitTabPane parent;
        DndTabPane(SplitTabPane parent, EditorPane node) {
            this.parent = parent;
            getChildren().addAll(tabPane, marker);
            marker.setStroke(Color.DARKORANGE);
            marker.setManaged(false);
            tabPane.focusedProperty().addListener(this::handleFocused);
            setOnDragOver(this::handleDragOver);
            setOnDragDropped(this::handleDragDropped);
            setOnDragExited(this::handleDragExited);
            setOnDragDone(this::handleDragDone);
            add(node);
        }
        DndTabPane parentWith(SplitTabPane parent) {
            this.parent = parent;
            return this;
        }
        void add(EditorPane node) {
            var label = new Label(node.fileNameProperty().get());
            label.setOnDragDetected(this::handleTabDragDetected);
            var tab = new Tab();
            tab.setContent(node);
            tab.setGraphic(label);
            tab.setOnClosed(this::handleOnTabClosed);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            node.fileNameProperty().addListener(
                    (ob, o, n) -> label.setText(n));
            node.setNewOpenHandler(path -> add(new EditorPane()));
        }
        void handleFocused(ObservableValue<? extends Boolean> ob, Boolean o, Boolean focused) {
            if (focused) {
                activePane.set(this);
                if (!tabPane.getTabs().isEmpty()) {
                    ((EditorPane) tabPane.getSelectionModel().getSelectedItem().getContent()).focus();
                }
            }
        }
        private void handleOnTabClosed(Event e) {
            if (tabPane.getTabs().isEmpty()) {
                add(new EditorPane());
            }
        }
        private void handleTabDragDetected(MouseEvent e) {
            if (e.getSource() instanceof Label label) {
                Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.put(tabMove, String.valueOf(System.identityHashCode(label)));
                Image image = tabImage(label);
                db.setDragView(image, image.getWidth(), -image.getHeight());
                db.setContent(cc);
                Tab tab = tabPane.getTabs().stream().filter(t -> t.getGraphic().equals(label)).findFirst().get();
                draggedTab.set(tab);
            }
        }
        private void handleDragOver(DragEvent e) {
            Dragboard db = e.getDragboard();
            Tab dragged = draggedTab.get();
            if (!db.hasContent(tabMove) || dragged == null) return;
            Bounds bounds = tabPane.getLayoutBounds();
            marker.setX(0.0);
            marker.setY(0.0);
            marker.setWidth(bounds.getWidth());
            marker.setHeight(bounds.getHeight());
            marker.setVisible(true);
            switch (dropPoint(this, e)) {
                case HEADER -> {}
                case RIGHT -> {
                    marker.setX(bounds.getCenterX());
                    marker.setWidth(bounds.getWidth() / 2);
                }
                case LEFT -> {
                    marker.setWidth(bounds.getWidth() / 2);
                }
                case BOTTOM -> {
                    marker.setY(bounds.getCenterY());
                    marker.setHeight(bounds.getHeight() / 2);
                }
                case TOP -> {
                    marker.setHeight(bounds.getHeight() / 2);
                }
            }
        }
        private void handleDragDropped(DragEvent e) {
        }
        private void handleDragExited(DragEvent e) {
            marker.setVisible(false);
        }
        private void handleDragDone(DragEvent e) {
            marker.setVisible(false);
            draggedTab.set(null);
        }
    }

    enum DropPoint { HEADER, TOP, RIGHT, BOTTOM, LEFT, ANY; }

    private static DropPoint dropPoint(Node node, DragEvent e) {
        Bounds paneBounds = node.localToScreen(node.getBoundsInLocal());
        double w = paneBounds.getWidth() / 4;
        double h = paneBounds.getHeight() / 4;
        if (new BoundingBox(
                paneBounds.getMinX() - 25,
                paneBounds.getMinY(),
                paneBounds.getWidth(),
                25 * 2).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.HEADER;
        } else if (new BoundingBox(
                paneBounds.getMaxX() - w,
                paneBounds.getMinY() + h,
                w,
                paneBounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.RIGHT;
        } else if (new BoundingBox(
                paneBounds.getMinX() + w,
                paneBounds.getMaxY() - h,
                paneBounds.getWidth() - w * 2,
                h).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.BOTTOM;
        } else if (new BoundingBox(
                paneBounds.getMaxX() - w,
                paneBounds.getMinY() + h,
                w,
                paneBounds.getHeight() - h * 2).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.LEFT;
        } else if (new BoundingBox(
                paneBounds.getMinX() + w,
                paneBounds.getMinY(),
                paneBounds.getWidth() - w * 2,
                h).contains(e.getScreenX(), e.getScreenY())) {
            return DropPoint.TOP;
        } else {
            return DropPoint.ANY;
        }
    }

    private static Image tabImage(Node node) {
        node = tabNode(node);
        var snapshotParams = new SnapshotParameters();
        snapshotParams.setFill(Color.TRANSPARENT);
        return node.snapshot(snapshotParams, null);
    }

    private static Node tabNode(Node node) {
        for (;;) {
            node = node.getParent();
            if (Objects.equals(
                    node.getClass().getSimpleName(),
                    "TabHeaderSkin")) return node;
        }
    }

}
