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
import com.mammb.code.editor.core.layout.LayoutView;
import com.mammb.code.editor.core.text.Text;
import com.mammb.code.editor.core.Caret.Point;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class PlainEditorModel implements EditorModel {
    double marginTop = 5, marginLeft = 5;
    private final Content content;
    private final LayoutView screen;
    private final CaretGroup carets = CaretGroup.of();

    public PlainEditorModel(Content content, FontMetrics fm) {
        this.content = content;
        this.screen = LayoutView.of(content, fm);
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
    public void moveCaretRight(boolean withSel) {
        for (Caret c : carets.carets()) {
            var text = screen.textAt(c.point().row());
            if (text == null) continue;
            int next = text.indexRight(c.point().col());
            if (next <= 0) {
                c.at(c.point().row() + 1, 0);
            } else {
                c.at(c.point().row(), next);
            }
        }
    }

    @Override
    public void moveCaretLeft(boolean withSel) {
        for (Caret c : carets.carets()) {
            if (c.isZero()) continue;
            if (c.point().col() == 0) {
                var text = screen.textAt(c.point().row() - 1);
                c.at(c.point().row() - 1, text.textLength());
            } else {
                var text = screen.textAt(c.point().row());
                int next = text.indexLeft(c.point().col());
                c.at(c.point().row(), next);
            }
        }
    }

    @Override
    public void moveCaretDown(boolean withSel) {
        for (Caret c : carets.carets()) {
            int line = screen.rowToLine(c.point().row(), c.point().col());
            if (line == screen.lineSize()) continue;
            double x = (c.vPos() < 0)
                    ? screen.colToXOnLayout(line, c.point().col())
                    : c.vPos();
            line++;
            c.at(screen.lineToRow(line), screen.xToCol(line, x), x);
        }
    }

    @Override
    public void moveCaretUp(boolean withSel) {
        for (Caret c : carets.carets()) {
            int line = screen.rowToLine(c.point().row(), c.point().col());
            if (line == 0) continue;
            double x = (c.vPos() < 0)
                    ? screen.colToXOnLayout(line, c.point().col())
                    : c.vPos();
            line--;
            c.at(screen.lineToRow(line), screen.xToCol(line, x), x);
        }
    }

    @Override
    public void click(double x, double y) {
        Caret c = carets.unique();
        int line = screen.yToLineOnView(y - marginTop);
        c.at(screen.lineToRow(line), screen.xToCol(line, x));
    }
    @Override
    public void clickDouble(double x, double y) {
        Caret c = carets.getFirst();
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
