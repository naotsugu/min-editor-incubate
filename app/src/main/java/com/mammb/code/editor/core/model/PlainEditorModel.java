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

import com.mammb.code.editor.core.Caret;
import com.mammb.code.editor.core.CaretGroup;
import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.layout.ScreenLayout;
import com.mammb.code.editor.core.text.Text;
import com.mammb.code.editor.core.Caret.Point;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class PlainEditorModel implements EditorModel {
    double marginTop = 5, marginLeft = 5;
    private final Content content;
    private final ScreenLayout screen;
    private final CaretGroup carets = CaretGroup.of();

    public PlainEditorModel(Content content, FontMetrics fm) {
        this.content = content;
        this.screen = ScreenLayout.of(content, fm);
    }

    @Override
    public void draw(Draw draw) {
        draw.clear();
        double y = 0;
        for (Text text : screen.texts()) {
            double x = 0;
            draw.text(text.value(), x + marginLeft, y + marginTop, text.width(), List.of());
            x += text.width();
            y += text.height();
        }
        for (Point p : carets.points()) {
            screen.locationOn(p.row(), p.col())
                  .ifPresent(loc -> draw.caret(loc.x() + marginLeft, loc.y() + marginTop));
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
    public void moveCaretRight() {
        for (Caret caret : carets.carets()) {
            var text = screen.text(caret.point().row());
            if (text == null) continue;
            int next = text.indexRight(caret.point().col());
            if (next <= 0) {
                caret.at(caret.point().row() + 1, 0);
            } else {
                caret.at(caret.point().row(), next);
            }
        }
    }

    @Override
    public void moveCaretLeft() {
        for (Caret caret : carets.carets()) {
            if (caret.point().row() == 0 && caret.point().col() == 0) {
                return;
            } else if (caret.point().col() == 0) {
                var text = screen.text(caret.point().row() - 1);
                caret.at(caret.point().row() - 1, text.textLength());
            } else {
                var text = screen.text(caret.point().row());
                int next = text.indexLeft(caret.point().col());
                caret.at(caret.point().row(), next);
            }
        }
    }

    @Override
    public void moveCaretDown() {
        for (Caret caret : carets.carets()) {
            int line = screen.rowToLine(caret.point().row(), caret.point().col());
            if (line == screen.lineSize()) continue;
            double x = (caret.vPos() < 0)
                    ? screen.xOnLayout(line, caret.point().col())
                    : caret.vPos();
            line++;
            caret.at(screen.lineToRow(line), screen.xToCol(line, x), x);
        }
    }

    @Override
    public void moveCaretUp() {
        for (Caret caret : carets.carets()) {

        }
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
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            var pos = content.insert(caret.point(), text);
            screen.refreshBuffer(caret.point().row(), pos.row() + 1);
            caret.at(pos);
        } else {

        }
    }
    @Override
    public void delete() {
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            var del = content.delete(caret.point());
            screen.refreshBuffer(caret.point().row(), caret.point().row() + 1);
        } else {

        }
    }
    @Override
    public void backspace() {
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            var pos = content.backspace(caret.point());
            screen.refreshBuffer(pos.row(), caret.point().row() + 1);
        } else {

        }
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
