package com.mammb.code.editor.core.layout;

import com.mammb.code.editor.core.text.RowText;
import com.mammb.code.editor.core.text.Text;
import java.util.Arrays;

public interface Layout {
    void setWidth(double width);
    void refresh(int line);
    void refreshRow(int start, int end);
    Text text(int line);
    RowText rowText(int line);
    double lineHeight();
    int lineSize();

    default double x(int line, int col) {
        double[] ad = text(line).advances();
        return Arrays.stream(ad, 0, Math.min(col, ad.length)).sum();
    }

    default double y(int line) {
        return line * lineHeight();
    }

    default int line(double y) {
        return (int) (y / lineHeight());
    }

    default int col(double x, double y) {
        double[] ad = text(line(y)).advances();
        int col = 0;
        for ( ; col < ad.length; col++) {
            if ((y - ad[col]) < 0) break;
            y -= ad[col];
        }
        return col;
    }

}
