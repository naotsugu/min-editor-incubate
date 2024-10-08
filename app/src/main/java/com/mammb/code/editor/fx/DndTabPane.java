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
import java.util.concurrent.atomic.AtomicReference;

public class DndTabPane extends StackPane {

    private static final AtomicReference<Tab> draggedTab = new AtomicReference<>();

    private static final DataFormat tabMove = new DataFormat("DnDTabPane:tabMove");
    private static final Polyline marker = new Polyline();
    private final TabPane tabPane = new TabPane();

    public DndTabPane() {
        getChildren().addAll(tabPane, marker);
        tabPane.focusedProperty().addListener(this::handleFocused);
        marker.setStroke(Color.DARKORANGE);
        marker.setManaged(false);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragExited(this::handleDragExited);
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
            Tab tab = tabPane.getTabs().stream().filter(t -> t.getGraphic().equals(label)).findFirst().get();
            draggedTab.set(tab);
        }
    }

    private void handleDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        Tab dragged = draggedTab.get();
        if (db.hasContent(tabMove) && dragged != null) {
            marker.getPoints().clear();
            Bounds bounds = tabPane.getLayoutBounds();
            if (isTabHeaderIn(e)) {
                Tab tab = tabSelectOn(e);
                if (tab == null) {
                    Node tabNode = tabNode(tabPane.getTabs().getLast().getGraphic());
                    Bounds ins = screenToLocal(tabNode.localToScreen(tabNode.getBoundsInLocal()));
                    marker.getPoints().addAll(ins.getMaxX() + 2, 0.0, ins.getMaxX() + 2, ins.getHeight());
                } else {
                    Node tabNode = tabNode(tab.getGraphic());
                    Bounds ins = screenToLocal(tabNode.localToScreen(tabNode.getBoundsInLocal()));
                    marker.getPoints().addAll(ins.getMinX() + 2, 0.0, ins.getMinX() + 2, ins.getHeight());
                }
                e.acceptTransferModes(TransferMode.MOVE);
            } else if (isRightIn(e)) {
                marker.getPoints().addAll(
                        bounds.getCenterX(), 0.0,
                        bounds.getMaxX(), 0.0,
                        bounds.getMaxX(), bounds.getMaxY(),
                        bounds.getCenterX(), bounds.getMaxY(),
                        bounds.getCenterX(), 0.0
                );
                e.acceptTransferModes(TransferMode.MOVE);
            } else if (isLeftIn(e)) {
                marker.getPoints().addAll(
                        0.0, 0.0,
                        bounds.getCenterX(), 0.0,
                        bounds.getCenterX(), bounds.getMaxY(),
                        0.0, bounds.getMaxY(),
                        0.0, 0.0
                );
                e.acceptTransferModes(TransferMode.MOVE);
            } else if (isBottomIn(e)) {
                marker.getPoints().addAll(
                        0.0, bounds.getCenterY(),
                        bounds.getMaxX(), bounds.getCenterY(),
                        bounds.getMaxX(), bounds.getMaxY(),
                        bounds.getMinX(), bounds.getMaxY(),
                        0.0, bounds.getCenterY()
                );
                e.acceptTransferModes(TransferMode.MOVE);
            } else if (isTopIn(e)) {
                marker.getPoints().addAll(
                        0.0, 0.0,
                        bounds.getMaxX(), 0.0,
                        bounds.getMaxX(), bounds.getCenterY(),
                        0.0, bounds.getCenterY(),
                        0.0, 0.0
                );
                e.acceptTransferModes(TransferMode.MOVE);
            }
        }
    }

    private void handleDragDropped(DragEvent e) {
        var db = e.getDragboard();
        Tab dragged = draggedTab.get();
        if (db.hasContent(tabMove) && dragged != null) {
            marker.getPoints().clear();
            if (isRightIn(e)) {
                TabPane tabPane = dragged.getTabPane();
                if (tabPane.getTabs().size() < 2) {
                    return;
                }
                tabPane.getTabs().remove(dragged);
                DndTabPane newTabPane = new DndTabPane();
                newTabPane.add((EditorPane) dragged.getContent());
                ((DynSplitPane) tabPane.getParent().getParent().getParent().getParent())
                        .addRight(newTabPane);
            }
        }
    }

    private void handleDragExited(DragEvent e) {
        marker.getPoints().clear();
    }
    private void handleDragDone(DragEvent e) {
        marker.getPoints().clear();
        draggedTab.set(null);
    }

    private DynSplitPane parentDynSplitPane() {
        Node node = this;
        for (;;) {
            node = node.getParent();
            if (Objects.equals(node.getClass(), DynSplitPane.class))
                return (DynSplitPane) node;
        }
    }


    private Image tabImage(Node node) {
        node = tabNode(node);
        var snapshotParams = new SnapshotParameters();
        snapshotParams.setFill(Color.TRANSPARENT);
        return node.snapshot(snapshotParams, null);
    }

    private Node tabNode(Node node) {
        for (;;) {
            node = node.getParent();
            if (Objects.equals(
                    node.getClass().getSimpleName(),
                    "TabHeaderSkin")) return node;
        }
    }

    private Node tabHeader() {
        if (tabPane.getTabs().isEmpty()) return null;
        return tabPane.getTabs().getFirst().getGraphic().getParent().getParent();
    }

    private Tab tabSelectOn(DragEvent e) {
        for (Tab tab : tabPane.getTabs()) {
            Node tabNode = tabNode(tab.getGraphic());
            Bounds bounds = tabNode.localToScreen(tabNode.getBoundsInLocal());
            if (e.getScreenX() < bounds.getCenterX()) {
                return tab;
            }
        }
        return null;
    }

    private boolean isTabHeaderIn(DragEvent e) {
        Bounds paneBounds = localToScreen(getBoundsInLocal());
        double h = tabHeader().getLayoutBounds().getHeight();
        return new BoundingBox(
                paneBounds.getMinX() - 20,
                paneBounds.getMinY(),
                paneBounds.getWidth(),
                h + 20
        ).contains(e.getScreenX(), e.getScreenY());
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
