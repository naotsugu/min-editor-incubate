package code.editor;

import java.util.ArrayList;
import java.util.List;

public class LineText {
    private RowText parent;
    private int fromIndex;
    private int toIndex;

    LineText(RowText parent, int fromIndex, int toIndex) {
        this.parent = parent;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    float lineHeight() {
        return parent.lineHeight;
    }

    public int length() {
        return toIndex - fromIndex;
    }

    public boolean hasPrevLine() {
        return fromIndex > 0;
    }

    public boolean hasNext() {
        return parent.string.length() > toIndex;
    }

    public String text() {
        return parent.string.substring(fromIndex, toIndex);
    }

}
