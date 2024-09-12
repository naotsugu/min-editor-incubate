package com.mammb.code.editor.core.layout;

/**
 * Row Line interconversion.
 * @author Naotsugu Kobayashi
 */
public interface RowLineIc {
    int rowToFirstLine(int row);
    int rowToLastLine(int row);
    int rowToLine(int row, int col);
    int lineToRow(int line);
    int lineSize();
    int rowSize();
}
