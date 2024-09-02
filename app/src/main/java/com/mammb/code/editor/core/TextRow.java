package com.mammb.code.editor.core;

public interface TextRow extends ScreenLine {

    static TextRow of(int row, String text) {
        return new TextRowImpl(row, text);
    }

    class TextRowImpl implements TextRow {
        private final int row;
        private final String text;

        public TextRowImpl(int row, String text) {
            this.row = row;
            this.text = text;
        }

        @Override
        public int row() {
            return row;
        }
    }

}
