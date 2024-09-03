package com.mammb.code.editor.core.text;

public record RowRange(int row, int subLine, int fromIndex, int toIndex) {

    static RowRange empty = new RowRange(0, 0, 0, 0);

    public int length() {
        return toIndex - fromIndex;
    }

    boolean contains(int row, int col) {
        return this.row == row && this.fromIndex <= col && col < this.toIndex;
    }

}
