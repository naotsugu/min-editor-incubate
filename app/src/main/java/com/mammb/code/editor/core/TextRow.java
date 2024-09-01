package com.mammb.code.editor.core;

public interface TextRow extends ScreenLine {

    TextRow parent();

    class TextRowImpl implements TextRow {
        private final TextRow parent;
        private final RowMap map;

        public TextRowImpl(TextRow parent, RowMap map) {
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

    record RowMap(int subLine, int fromIndex, int toIndex) {
        static RowMap empty = new RowMap(0, 0, 0);
        int length() {
            return toIndex - fromIndex;
        }
        boolean contains(int col) {
            return this.fromIndex <= col && col < this.toIndex;
        }
    }
}
