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

public interface LayoutView extends RowLineIc {

    void setSize(double width, double height);
    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int line);
    void refreshBuffer(int startRow, int endRow);
    List<Text> texts();
    Text text(int line);
    Text textAt(int row);
    Optional<Loc> locationOn(int row, int col);
    double lineToYOnLayout(int line);
    double lineToYOnView(int line);
    double colToXOnLayout(int line, int col);
    double colToXOnView(int line, int col);
    int xToCol(int line, double x);
    int yToLineOnView(double y);

    static LayoutView of(Content content, FontMetrics fm) {
        Layout layout = new RowLayout(content, fm);
        return new BasicLayoutView(layout, content);
    }

    static LayoutView wrapOf(Content content, FontMetrics fm) {
        Layout layout = new WrapLayout(content, fm);
        return new BasicLayoutView(layout, content);
    }

    class BasicLayoutView implements LayoutView {
        private double width = 0, height = 0;
        private double xShift = 0;
        private int topLine = 0;
        private final List<Text> buffer = new ArrayList<>();
        private final Layout layout;
        private final Content content;

        public BasicLayoutView(Layout layout, Content content) {
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
            if (Math.abs(delta) < lineSizeOnView() * 2 / 3) {
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
        public Text text(int line) {
            return layout.text(line);
        }

        @Override
        public Text textAt(int row) {
            return layout.rowTextAt(row);
        }

        @Override
        public Optional<Loc> locationOn(int row, int col) {
            return layout.loc(row, col, topLine, topLine + lineSizeOnView())
                    .map(loc -> new Loc(loc.x(), loc.y() - lineToYOnLayout(topLine)));
        }

        @Override
        public double lineToYOnLayout(int line) {
            return layout.y(line);
        }

        @Override
        public double lineToYOnView(int line) {
            return lineToYOnLayout(line) - lineToYOnLayout(topLine);
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
        public double colToXOnLayout(int line, int col) {
            return layout.x(rowToLine(line, col), col);
        }

        @Override
        public double colToXOnView(int line, int col) {
            return colToXOnLayout(line, col) - xShift;
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

        @Override
        public int yToLineOnView(double y) {
            return topLine + (int) (y / layout.lineHeight());
        }

        private void fillBuffer() {
            buffer.clear();
            buffer.addAll(layout.texts(topLine, topLine + lineSizeOnView()));
        }

        public int lineSizeOnView() {
            return (int) Math.ceil(Math.max(0, height) / layout.lineHeight());
        }

    }
}