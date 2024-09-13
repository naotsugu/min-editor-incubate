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

import code.editor.syntax.Syntax;
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
    private final LayoutView view;
    private final Syntax syntax;
    private final CaretGroup carets = CaretGroup.of();

    public PlainEditorModel(Content content, FontMetrics fm, Syntax syntax) {
        this.content = content;
        this.view = LayoutView.of(content, fm);
        this.syntax = syntax;
    }

    @Override
    public void draw(Draw draw) {
        draw.clear();
        double y = 0;
        for (Text text : view.texts()) {
            double x = 0;
            draw.text(text.value(), x + marginLeft, y + marginTop, text.width(), List.of());
            x += text.width();
            y += text.height();
        }
        for (Point p : carets.points()) {
            view.locationOn(p.row(), p.col())
                  .ifPresent(loc -> draw.caret(loc.x() + marginLeft, loc.y() + marginTop));
        }
    }
    @Override
    public void setSize(double width, double height) {
        view.setSize(width, height);
    }
    @Override
    public void scrollNext(int delta) {
        view.scrollNext(delta);
    }
    @Override
    public void scrollPrev(int delta) {
        view.scrollPrev(delta);
    }

    @Override
    public void scrollAt(int line) {
        view.scrollAt(line);
    }

    @Override
    public void moveCaretRight(boolean withSel) {
        for (Caret c : carets.carets()) {
            var text = view.textAt(c.point().row());
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
                var text = view.textAt(c.point().row() - 1);
                c.at(c.point().row() - 1, text.textLength());
            } else {
                var text = view.textAt(c.point().row());
                int next = text.indexLeft(c.point().col());
                c.at(c.point().row(), next);
            }
        }
    }

    @Override
    public void moveCaretDown(boolean withSel) {
        for (Caret c : carets.carets()) {
            int line = view.rowToLine(c.point().row(), c.point().col());
            if (line == view.lineSize()) continue;
            double x = (c.vPos() < 0)
                    ? view.colToXOnLayout(line, c.point().col())
                    : c.vPos();
            line++;
            c.at(view.lineToRow(line), view.xToCol(line, x), x);
        }
    }

    @Override
    public void moveCaretUp(boolean withSel) {
        for (Caret c : carets.carets()) {
            int line = view.rowToLine(c.point().row(), c.point().col());
            if (line == 0) continue;
            double x = (c.vPos() < 0)
                    ? view.colToXOnLayout(line, c.point().col())
                    : c.vPos();
            line--;
            c.at(view.lineToRow(line), view.xToCol(line, x), x);
        }
    }

    @Override
    public void click(double x, double y) {
        Caret c = carets.unique();
        int line = view.yToLineOnView(y - marginTop);
        c.at(view.lineToRow(line), view.xToCol(line, x));
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
            view.refreshBuffer(caret.point().row(), pos.row() + 1);
            caret.at(pos);
        } else {

        }
    }
    @Override
    public void delete() {
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            var del = content.delete(caret.point());
            view.refreshBuffer(caret.point().row(), caret.point().row() + 1);
        } else {

        }
    }
    @Override
    public void backspace() {
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            var pos = content.backspace(caret.point());
            view.refreshBuffer(pos.row(), caret.point().row() + 1);
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
    public void pasteFromClipboard() {

    }

    @Override
    public void copyToClipboard() {

    }

    @Override
    public void cutToClipboard() {

    }

    @Override
    public boolean isModified() {
        return content.isModified();
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