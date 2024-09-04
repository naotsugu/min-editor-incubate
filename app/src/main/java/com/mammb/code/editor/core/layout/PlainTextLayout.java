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

import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.text.RowText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlainTextLayout implements TextLayout {

    private double width = 0, height = 0;
    private double xShift = 0;
    private final double lineHeight;
    private final List<RowText> viewBuffer = new ArrayList<>();
    private final Content content;
    private final FontMetrics fm;

    public PlainTextLayout(Content content, FontMetrics fm) {
        this.content = content;
        this.fm = fm;
        this.lineHeight = fm.getLineHeight();
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        fillBuffer();
    }

    private void fillBuffer() {
        int top = lineTop();
        viewBuffer.clear();
        for (int i = top; i < top + viewLineSize(); i++) {
            viewBuffer.add(rowText(i));
        }
    }

    private int lineTop() {
        return viewBuffer.isEmpty() ? 0 : viewBuffer.getFirst().row();
    }

    private RowText rowText(int row) {
        return RowText.of(row, content.getText(row), fm);
    }

    public int viewLineSize() {
        return (int) Math.ceil(Math.max(0, height) / lineHeight);
    }

    private double y(int line) {
        return line * lineHeight;
    }

    private double yInView(int line) {
        return (line - lineTop()) * lineHeight;
    }

    private double x(int row, int col) {
        double[] ad = rowText(row).advances();
        return Arrays.stream(ad, 0, Math.min(col, ad.length)).sum();
    }

    private double xInView(int row, int col) {
        return x(row, col) - xShift;
    }
}