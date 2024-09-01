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
package com.mammb.code.editor.core;

import java.util.Objects;

/**
 * The Caret.
 * @author Naotsugu Kobayashi
 */
public interface Caret extends Comparable<Caret>{

    Point point();
    void mark();
    void clearMark();
    boolean isMarked();
    boolean isFloating();
    Range markedRange();

    static Caret of() {
        return new CaretImpl();
    }

    class CaretImpl implements Caret {

        private final PointMut point = new PointMut(0, 0);
        private Point mark;
        private double vPos;
        private boolean floating;

        @Override
        public void mark() {
            mark = new PointMut(point.row(), point.col());
        }

        @Override
        public void clearMark() {
            mark = null;
        }

        @Override
        public boolean isMarked() {
            return mark != null;
        }

        @Override
        public boolean isFloating() {
            return floating;
        }

        @Override
        public Range markedRange() {
            return isMarked() ? new Range(new PointRec(point), mark) : null;
        }

        @Override
        public int compareTo(Caret that) {
            return this.point().compareTo(that.point());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Caret caret = (Caret) o;
            return Objects.equals(point, caret.point());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(point);
        }

        @Override
        public Point point() {
            return point;
        }
    }

    interface Point extends Comparable<Point> {
        int row();
        int col();
        @Override
        default int compareTo(Point that) {
            int c = Integer.compare(this.row(), that.row());
            if (c == 0) {
                return Integer.compare(this.col(), that.col());
            } else {
                return c;
            }
        }
    }

    record PointRec(int row, int col) implements Point {
        PointRec(Point p) { this(p.row(), p.col()); }
    }

    class PointMut implements Point {

        private int row, col;

        PointMut(int row, int col) {
            this.row = row;
            this.col = col;
        }

        void at(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int row() {
            return row;
        }

        public int col() {
            return col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return row == point.row() && col == point.col();
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

    }

    record Range(Point start, Point end) { }

}