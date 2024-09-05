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
import com.mammb.code.editor.core.text.RowRange;
import com.mammb.code.editor.core.text.RowText;
import com.mammb.code.editor.core.text.SubText;
import com.mammb.code.editor.core.text.Text;
import java.util.ArrayList;
import java.util.List;

public class WrapTextLayout implements TextLayout {

    private double wrapWidth = 0;
    private double height = 0;
    private int lineTop = 0;
    private final double lineHeight;
    private final Content content;
    private final FontMetrics fm;
    private final List<SubText> viewBuffer = new ArrayList<>();
    private final List<RowRange> lineIndexes = new ArrayList<>();

    public WrapTextLayout(Content content, FontMetrics fm) {
        this.content = content;
        this.fm = fm;
        this.lineHeight = fm.getLineHeight();
    }

    @Override
    public void setSize(double width, double height) {
        viewBuffer.clear();
        lineIndexes.clear();
        for (int i = 0; i < content.rows(); i++) {
            var row = RowText.of(i, content.getText(i), fm);

            var subs = SubText.of(row, width);
            for (int j = 0; j < subs.size(); j ++) {
                var sub = subs.get(j);
                if (lineTop <= lineIndexes.size() && lineIndexes.size() < lineTop + viewLineSize()) {
                    viewBuffer.add(sub);
                }
                lineIndexes.add(new RowRange(sub.row(), j, sub.fromIndex(), sub.toIndex()));
            }
        }

        this.wrapWidth = width;
    }

    public int viewLineSize() {
        return (int) Math.ceil(Math.max(0, height) / lineHeight);
    }

    @Override
    public List<? extends Text> viewBuffer() {
        return List.of();
    }

}
