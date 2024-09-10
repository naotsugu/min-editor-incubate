/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor.core.layout;

import com.mammb.code.editor.core.text.RowText;
import com.mammb.code.editor.core.text.Text;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

interface Layout {
    void setWidth(double width);
    void refresh(int line);
    void refreshRow(int start, int end);
    Text text(int line);
    List<Text> texts(int startLine, int endLine);
    RowText rowText(int line);
    RowText rowTextAt(int row);
    double lineHeight();
    int lineSize();
    int rowToLine(int row);
    int lineToRow(int line);

    /**
     *
     * @param row
     * @param col
     * @param startLine the limit
     * @param endLine the limit
     * @return
     */
    Optional<Loc> loc(int row, int col, int startLine, int endLine);

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
