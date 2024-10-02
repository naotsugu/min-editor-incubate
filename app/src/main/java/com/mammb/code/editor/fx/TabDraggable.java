package com.mammb.code.editor.fx;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import java.util.UUID;

public class TabDraggable extends Tab {
    private Label content;
    private UUID uuid = UUID.randomUUID();

    public TabDraggable(String text) {
        content = new Label(text);
        setGraphic(content);
        content.setOnDragDetected(this::handleDragDetected);
        content.setOnDragDone(this::handleDragDone);
    }

    public void setTextDraggable(String text) {
        content.setText(text);
    }

    private void handleDragDetected(MouseEvent e) {
        Dragboard dragboard = content.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent cc = new ClipboardContent();
cc.putString("");
        dragboard.setContent(cc);
        dragboard.setDragView(content.snapshot(null, null));
    }

    private void handleDragDone(DragEvent e) {
        System.out.println(e);

    }

}
