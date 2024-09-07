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
import com.mammb.code.editor.core.text.Text;
import java.util.Optional;

public class RowLayout implements Layout {
    private final double lineHeight;
    private final Content content;
    private final FontMetrics fm;

    public RowLayout(Content content, FontMetrics fm) {
        this.lineHeight = fm.getLineHeight();
        this.content = content;
        this.fm = fm;
    }

    public void setWidth(double width) {
        // nothing to do
    }

    public void refresh(int line) {
        // nothing to do
    }

    @Override
    public void refreshRow(int start, int end) {
        // nothing to do
    }

    public Text text(int line) {
        return rowText(line);
    }

    public RowText rowText(int line) {
        return RowText.of(line, content.getText(line), fm);
    }

    @Override
    public double lineHeight() {
        return lineHeight;
    }

    @Override
    public int lineSize() {
        return content.rows();
    }

    @Override
    public Optional<Loc> loc(int row, int col, int startLine, int endLine) {
        if (startLine <= row && row < endLine) {
            return Optional.of(new Loc(x(row, col),y(row)));
        } else {
            return Optional.empty();
        }
    }

}
