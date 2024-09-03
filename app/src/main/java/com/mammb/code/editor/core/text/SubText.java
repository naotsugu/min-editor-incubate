package com.mammb.code.editor.core.text;

import java.util.ArrayList;
import java.util.List;

public record SubText(RowText parent,
        int subLine, int fromIndex, int toIndex, double width)
        implements Text {

    public static List<SubText> of(RowText rowText, double width) {
        if (width <= 0) {
            return List.of(new SubText(rowText, 0, 0, rowText.length(), rowText.width()));
        }
        double w = 0;
        int fromIndex = 0;
        List<SubText> subs = new ArrayList<>();
        double[] advances = rowText.advances();
        for (int i = 0; i < rowText.length(); i++) {
            double advance = advances[i];
            if (advance <= 0) continue;
            if (w + advance > width) {
                subs.add(new SubText(rowText, subs.size(), fromIndex, i, w));
                w = 0;
                fromIndex = i;
            }
            w += advance;
        }
        subs.add(new SubText(rowText, subs.size(), fromIndex, rowText.length(), w));
        return subs;
    }

    @Override
    public int row() {
        return parent().row();
    }

    @Override
    public String text() {
        return parent().text().substring(fromIndex, toIndex);
    }

    @Override
    public double height() {
        return parent().height();
    }
}
