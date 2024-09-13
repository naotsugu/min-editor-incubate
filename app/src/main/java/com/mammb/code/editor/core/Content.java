/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.editor.core;

import com.mammb.code.piecetable.TextEdit;
import com.mammb.code.editor.core.Caret.Point;
import com.mammb.code.editor.core.Caret.PointRec;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface Content {

    Point insert(Point point, String text);
    String delete(Point point);
    Point backspace(Point point);

    /**
     * Undo.
     * @return the undo position
     */
    List<Point> undo();

    /**
     * Redo.
     * @return the redo position
     */
    List<Point> redo();


    String getText(int row);
    String getText(Point start, Point end);
    int rows();
    Optional<Path> path();
    void save(Path path);
    boolean isModified();

    static Content of() {
        return new ContentImpl();
    }
    static Content of(Path path) {
        return new ContentImpl(path);
    }

    class ContentImpl implements Content {
        private final TextEdit textEdit;

        public ContentImpl() {
            this.textEdit = TextEdit.of();
        }
        public ContentImpl(Path path) {
            this.textEdit = TextEdit.of(path);
        }

        @Override
        public Point insert(Point point, String text) {
            var pos = textEdit.insert(point.row(), point.col(), text);
            return new PointRec(pos.row(), pos.col());
        }

        @Override
        public String delete(Point point) {
            return textEdit.delete(point.row(), point.col());
        }
        @Override
        public Point backspace(Point point) {
            var pos = textEdit.backspace(point.row(), point.col());
            return new PointRec(pos.row(), pos.col());
        }
        @Override
        public List<Point> undo() {
            return textEdit.undo().stream().map(p -> Point.of(p.row(), p.col())).toList();
        }

        @Override
        public List<Point> redo() {
            return textEdit.redo().stream().map(p -> Point.of(p.row(), p.col())).toList();
        }

        @Override
        public String getText(int row) {
            return textEdit.getText(row);
        }

        @Override
        public String getText(Point start, Point end) {
            return textEdit.getText(start.row(), start.col(), end.row(), end.col());
        }

        @Override
        public int rows() {
            return textEdit.rows();
        }

        @Override
        public Optional<Path> path() {
            return Optional.ofNullable(textEdit.path());
        }

        @Override
        public void save(Path path) {
            textEdit.save(path);
        }

        @Override
        public boolean isModified() {
            textEdit.flush();
            return textEdit.hasUndoRecord();
        }
    }
}
