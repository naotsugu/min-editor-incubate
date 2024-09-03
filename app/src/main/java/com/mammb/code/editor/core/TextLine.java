package com.mammb.code.editor.core;

import com.mammb.code.editor.core.text.RowText;

public interface TextLine {

    RowText parent();

    class TextLineImpl implements TextLine {
        private final RowText parent;
        private final RowMap map;

        public TextLineImpl(RowText parent, RowMap map) {
            this.parent = parent;
            this.map = map;
        }

        @Override
        public RowText parent() {
            return parent;
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
