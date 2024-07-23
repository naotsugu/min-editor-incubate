package code.editor;

import code.editor.javafx.FontMetrics;

public class RowText {
    private String string;
    private CpAttr[] cpAttrs;
    private float lineHeight;
    private float ascent;

    public RowText(String string, float lineHeight, float ascent, CpAttr[] cpAttrs) {
        this.string = string;
        this.lineHeight = lineHeight;
        this.ascent = ascent;
        this.cpAttrs = cpAttrs;
    }

    public static RowText of(String string, FontMetrics fm) {
        CpAttr[] cpAttrs = new CpAttr[string.codePointCount(0, string.length())];
        int j = 0;
        for (int i = 0; i < string.length(); i++) {
            char ch1 = string.charAt(i);
            char ch2 = Character.isHighSurrogate(ch1)
                    ? string.charAt(++i)
                    : (char) 0;
            cpAttrs[j++] = new CpAttr(true, fm.getAdvance(ch1, ch2));
        }
        return new RowText(string, fm.getLineHeight(), fm.getMaxAscent(), cpAttrs);
    }

    record CpAttr(boolean wide, double advance) {}

}
