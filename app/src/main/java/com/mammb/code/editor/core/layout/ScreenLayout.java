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
package com.mammb.code.editor.core.layout;

import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ScreenLayout extends RowLineIc {

    void setSize(double width, double height);
    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int line);
    void refreshBuffer(int startRow, int endRow);
    List<Text> texts();
    Text text(int row);
    Optional<Loc> locationOn(int row, int col);
    double xOnLayout(int line, int col);
    int xToCol(int line, double x);

    static ScreenLayout of(Content content, FontMetrics fm) {
        Layout layout = new RowLayout(content, fm);
        return new BasicScreenLayout(layout, content);
    }

    static ScreenLayout wrapOf(Content content, FontMetrics fm) {
        Layout layout = new WrapLayout(content, fm);
        return new BasicScreenLayout(layout, content);
    }

    class BasicScreenLayout implements ScreenLayout {
        private double width = 0, height = 0;
        private double xShift = 0;
        private int topLine = 0;
        private final List<Text> buffer = new ArrayList<>();
        private final Layout layout;
        private final Content content;

        public BasicScreenLayout(Layout layout, Content content) {
            this.layout = layout;
            this.content = content;
        }

        @Override
        public void setSize(double width, double height) {
            layout.setWidth(width);
            this.width = width;
            this.height = height;
            fillBuffer();
        }

        @Override
        public void scrollNext(int delta) {
            scrollAt(topLine + delta);
        }

        @Override
        public void scrollPrev(int delta) {
            scrollAt(topLine - delta);
        }

        @Override
        public void scrollAt(int line) {
            line = Math.clamp(line, 0, layout.lineSize() - 1);
            int delta = line - topLine;
            if (delta == 0) return;
            if (Math.abs(delta) < screenLineSize() * 2 / 3) {
                topLine = line;
                if (delta > 0) {
                    // scroll next
                    buffer.subList(0, delta).clear();
                    buffer.addAll(layout.texts(line + buffer.size(), line + buffer.size() + delta));
                } else {
                    // scroll prev
                    buffer.subList(buffer.size() + delta, buffer.size()).clear();
                    buffer.addAll(0, layout.texts(line, line - delta));
                }
            } else {
                this.topLine = line;
                fillBuffer();
            }
        }

        @Override
        public void refreshBuffer(int startRow, int endRow) {
            layout.refreshAt(startRow, endRow);
            fillBuffer();// TODO optimize
        }

        @Override
        public List<Text> texts() {
            return buffer;
        }

        @Override
        public Text text(int row) {
            return layout.rowTextAt(row);
        }

        @Override
        public Optional<Loc> locationOn(int row, int col) {
            return layout.loc(row, col, topLine, topLine + screenLineSize())
                    .map(loc -> new Loc(loc.x(), loc.y() - topY()));
        }

        @Override
        public int lineSize() {
            return layout.lineSize();
        }

        @Override
        public int rowSize() {
            return layout.rowSize();
        }

        @Override
        public double xOnLayout(int line, int col) {
            return layout.x(rowToLine(line, col), col);
        }

        @Override
        public int rowToFirstLine(int row) {
            return layout.rowToFirstLine(row);
        }

        @Override
        public int rowToLastLine(int row) {
            return layout.rowToLastLine(row);
        }

        @Override
        public int rowToLine(int row, int col) {
            return layout.rowToLine(row, col);
        }

        @Override
        public int lineToRow(int line) {
            return layout.lineToRow(line);
        }

        @Override
        public int xToCol(int line, double x) {
            return layout.xToCol(line, x);
        }

        private void fillBuffer() {
            buffer.clear();
            buffer.addAll(layout.texts(topLine, topLine + screenLineSize()));
        }

        public int screenLineSize() {
            return (int) Math.ceil(Math.max(0, height) / layout.lineHeight());
        }

        private double topY() {
            return topLine * layout.lineHeight();
        }

    }
}
