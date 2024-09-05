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

import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.Draw;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.StackPane;
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
    private final ScrollBar vScroll = new ScrollBar();
    private final ScrollBar hScroll = new ScrollBar();

    public EditorPane() {
        canvas = new Canvas(640, 480);
        draw = new FxDraw(canvas.getGraphicsContext2D());
        model = EditorModel.of(Path.of("app/build.gradle.kts"), draw.fontMetrics());
        getChildren().add(canvas);

        layoutBoundsProperty().addListener(this::handleLayoutBoundsChanged);
    }

    private void handleLayoutBoundsChanged(
            ObservableValue<? extends Bounds> ob, Bounds o, Bounds n) {
        canvas.setWidth(n.getWidth());
        canvas.setHeight(n.getHeight());
        model.setSize(n.getWidth(), n.getHeight());
        draw();
    }

    private void draw() {
        model.draw(draw);
    }

}
