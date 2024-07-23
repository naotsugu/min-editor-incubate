package code.editor;

import code.editor.javafx.FontMetrics;
import java.util.ArrayList;
import java.util.List;

public class RowText {
    private int row;
    final String string;
    private CpAttr[] cpAttrs;
    float lineHeight;
    float ascent;

    private RowText(int row, String string, float lineHeight, float ascent, CpAttr[] cpAttrs) {
        this.row = row;
        this.string = string;
        this.lineHeight = lineHeight;
        this.ascent = ascent;
        this.cpAttrs = cpAttrs;
    }

    static RowText of(int row, String string, FontMetrics fm) {
        CpAttr[] cpAttrs = new CpAttr[string.codePointCount(0, string.length())];
        int j = 0;
        for (int i = 0; i < string.length(); i++) {
            char ch1 = string.charAt(i);
            char ch2 = Character.isHighSurrogate(ch1)
                    ? string.charAt(++i)
                    : (char) 0;
            cpAttrs[j++] = new CpAttr(true, fm.getAdvance(ch1, ch2));
        }
        return new RowText(row, string, fm.getLineHeight(), fm.getMaxAscent(), cpAttrs);
    }

    List<LineText> split(double width) {
        if (width <= 0) {
            return List.of(new LineText(this, 0, string.length()));
        }

        double w = 0;
        List<LineText> ret = new ArrayList<>();
        int fromIndex = 0;
        int toIndex = 0;
        for (CpAttr cp : cpAttrs) {
            if (w + cp.advance > width) {
                ret.add(new LineText(this, fromIndex, toIndex));
                fromIndex = toIndex;
                w = 0;
            }
            w += cp.advance;
            toIndex += cp.wide ? 2 : 1;
        }
        ret.add(new LineText(this, fromIndex, toIndex));
        return ret;
    }

    record CpAttr(boolean wide, double advance) {}

}
