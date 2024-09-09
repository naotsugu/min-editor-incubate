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
package com.mammb.code.editor.core.model;

import com.mammb.code.editor.core.CaretGroup;
import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.layout.ScreenBuffer;
import com.mammb.code.editor.core.text.Text;
import com.mammb.code.editor.core.Caret.Point;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class PlainEditorModel implements EditorModel {
    private final Content content;
    private final FontMetrics fm;
    private final ScreenBuffer screen;
    private final CaretGroup carets = CaretGroup.of();

    public PlainEditorModel(Content content, FontMetrics fm) {
        this.content = content;
        this.fm = fm;
        this.screen = ScreenBuffer.of(content, fm);
    }

    @Override
    public void draw(Draw draw) {
        draw.clear();

        double y = 0;
        for (Text text : screen.texts()) {
            double x = 0;
            draw.text(text.value(), x, y, text.width(), List.of());
            x += text.width();
            y += text.height();
        }
        for (Point p : carets.points()) {
            screen.locationOn(p.row(), p.col())
                  .ifPresent(loc -> draw.caret(loc.x(), loc.y()));
        }

    }
    @Override
    public void setSize(double width, double height) {
        screen.setSize(width, height);
    }
    @Override
    public void scrollNext(int delta) {
        screen.scrollNext(delta);
    }
    @Override
    public void scrollPrev(int delta) {
        screen.scrollPrev(delta);
    }

    @Override
    public void scrollAt(int line) {
        screen.scrollAt(line);
    }
    @Override
    public void click(double x, double y) {
        // TODO
    }
    @Override
    public void clickDouble(double x, double y) {
        // TODO
    }
    @Override
    public void clickTriple(double x, double y) {
        // TODO
    }
    @Override
    public void moveDragged(double x, double y) {
        // TODO
    }
    @Override
    public void input(String text) {
        // TODO
    }
    @Override
    public void delete() {
        // TODO
    }
    @Override
    public void backspace() {
        // TODO
    }
    @Override
    public void undo() {
        carets.at(content.undo());
    }
    @Override
    public void redo() {
        carets.at(content.redo());
    }
    @Override
    public Optional<Path> path() {
        return content.path();
    }
    @Override
    public void save(Path path) {
        content.save(path);
    }

}
