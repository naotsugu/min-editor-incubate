package com.mammb.code.editor.core;

public interface TextLine extends ScreenLine {

    TextRow parent();

    class TextLineImpl implements TextLine {
        private final TextRow parent;
        private final RowMap map;

        public TextLineImpl(TextRow parent, RowMap map) {
            this.parent = parent;
            this.map = map;
        }

        @Override
        public TextRow parent() {
            return parent;
        }

        @Override
        public int row() {
            return parent.row();
        }
    }

    record RowMap(int row, int subLine, int fromIndex, int toIndex) {
        static RowMap empty = new RowMap(0, 0, 0, 0);
        int length() {
            return toIndex - fromIndex;
        }
        boolean contains(int row, int col) {
            return this.row == row && this.fromIndex <= col && col < this.toIndex;
        }
    }
}
