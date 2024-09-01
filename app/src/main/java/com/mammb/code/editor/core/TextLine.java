package com.mammb.code.editor.core;

public interface TextLine extends ScreenLine {

    class TextLineImpl implements TextLine {
        private final int row;
        private final String text;

        public TextLineImpl(int row, String text) {
            this.row = row;
            this.text = text;
        }

        @Override
        public int row() {
            return row;
        }
    }

}
