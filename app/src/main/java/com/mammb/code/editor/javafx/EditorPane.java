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
package com.mammb.code.editor.javafx;

import com.mammb.code.editor.core.Action;
import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.Draw;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The EditorPane.
 * @author Naotsugu Kobayashi
 */
public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The draw. */
    private final Draw draw;
    /** The editor model. */
    private EditorModel model;
    /** The vertical scroll bar. */
    private final ScrollBar vScroll = new ScrollBar();
    /** The horizon scroll bar. */
    private final ScrollBar hScroll = new ScrollBar();

    /**
     * Constructor.
     */
    public EditorPane() {
        canvas = new Canvas(640, 480);
        canvas.setFocusTraversable(true);
        draw = new FxDraw(canvas.getGraphicsContext2D());
        model = EditorModel.of(Path.of("build.gradle.kts"), draw.fontMetrics());
        getChildren().add(canvas);

        layoutBoundsProperty().addListener(this::handleLayoutBoundsChanged);
        setOnScroll(this::handleScroll);
        setOnMouseClicked(this::handleMouseClicked);
        setOnMouseDragged(this::handleMouseDragged);
        setOnKeyPressed(this::handleKeyAction);
        setOnKeyTyped(this::handleKeyAction);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
    }

    private void handleLayoutBoundsChanged(
            ObservableValue<? extends Bounds> ob, Bounds o, Bounds n) {
        canvas.setWidth(n.getWidth());
        canvas.setHeight(n.getHeight());
        model.setSize(n.getWidth(), n.getHeight());
        draw();
    }

    private void handleScroll(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL && e.getDeltaY() != 0) {
            if (e.getDeltaY() < 0) {
                model.scrollNext((int) Math.min(5, Math.abs(e.getDeltaY())));
            } else {
                model.scrollPrev((int) Math.min(5, e.getDeltaY()));
            }
            draw();
        }
    }

    private void handleMouseClicked(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && e.getTarget() == canvas) {
            switch (e.getClickCount()) {
                case 1 -> model.click(e.getX(), e.getY());
                case 2 -> model.clickDouble(e.getX(), e.getY());
                case 3 -> model.clickTriple(e.getX(), e.getY());
            }
            draw();
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            model.moveDragged(e.getX(), e.getY());
            model.draw(draw);
        }
    }

    private void handleKeyAction(KeyEvent e) {
        execute(FxActions.of(e));
    }

    private void handleDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.MOVE);
        }
    }

    private void handleDragDropped(DragEvent e) {
        Dragboard board = e.getDragboard();
        if (board.hasFiles()) {
            var path = board.getFiles().stream().map(File::toPath)
                    .filter(Files::isReadable).filter(Files::isRegularFile).findFirst();
            if (path.isPresent()) {
                if (!canDiscardCurrent()) return;
                open(path.get());
                e.setDropCompleted(true);
                return;
            }
        }
        e.setDropCompleted(false);
    }

    private void openWithChooser() {
        if (!canDiscardCurrent()) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        if (model.path().isPresent()) {
            fc.setInitialDirectory(
                    model.path().get().toAbsolutePath().getParent().toFile());
        } else {
            fc.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
        }
        File file = fc.showOpenDialog(getScene().getWindow());
        if (file != null) open(file.toPath());
    }

    private void open(Path path) {
        String ext = Optional.of(path.getFileName().toString())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1))
                .orElse("");
        model = EditorModel.of(path, draw.fontMetrics());
        model.setSize(getWidth(), getHeight());
        draw();
    }

    private boolean canDiscardCurrent() {
        if (model.isModified()) {
            var result = FxDialog.confirmation(getScene().getWindow(),
                    "Are you sure you want to discard your changes?").showAndWait();
            return (result.isPresent() && result.get() == ButtonType.OK);
        } else {
            return true;
        }
    }

    private Action execute(Action action) {
        switch (action.type()) {
            case TYPED -> model.input(action.attr());
            case DELETE -> model.delete();
            case BACK_SPACE -> model.backspace();
            case UNDO -> model.undo();
            case REDO -> model.redo();
            case CARET_RIGHT -> model.moveCaretRight(false);
            case CARET_LEFT -> model.moveCaretLeft(false);
            case CARET_UP -> model.moveCaretUp(false);
            case CARET_DOWN -> model.moveCaretDown(false);
            case SELECT_CARET_RIGHT -> model.moveCaretRight(true);
            case SELECT_CARET_LEFT -> model.moveCaretLeft(true);
            case SELECT_CARET_UP -> model.moveCaretUp(true);
            case SELECT_CARET_DOWN -> model.moveCaretDown(true);
            case OPEN -> openWithChooser();
        }
        draw();
        return action;
    }

    private void draw() {
        model.draw(draw);
    }

}
