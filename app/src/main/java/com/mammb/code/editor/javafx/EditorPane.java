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
import javafx.scene.control.ScrollBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

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
    private final EditorModel model;
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
                //open(path.get());
                e.setDropCompleted(true);
                return;
            }
        }
        e.setDropCompleted(false);
    }

    private Action execute(Action action) {
        switch (action.type()) {
            case TYPED -> model.input(action.attr());
            case DELETE -> model.delete();
            case BACK_SPACE -> model.backspace();
            case UNDO -> model.undo();
            case REDO -> model.redo();
            case CARET_RIGHT -> model.moveCaretRight();
            case CARET_LEFT -> model.moveCaretLeft();
        }
        draw();
        return action;
    }

    private void draw() {
        model.draw(draw);
    }

}
