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
import com.mammb.code.editor.core.text.SubText;
import com.mammb.code.editor.core.text.Text;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class WrapLayout implements Layout {
    private double width = 0;
    private final double lineHeight;
    private final Content content;
    private final FontMetrics fm;
    private final List<SubRange> lines = new ArrayList<>();

    public WrapLayout(Content content, FontMetrics fm) {
        this.lineHeight = fm.getLineHeight();
        this.content = content;
        this.fm = fm;
    }

    public void setWidth(double width) {
        this.width = width;
        refresh(0);
    }

    public void refresh(int line) {
        lines.subList(line, lines.size()).clear();
        int i = 0;
        if (!lines.isEmpty()) {
            var range = lines.getLast();
            if (range.subLine() != range.subLines()) {
                lines.subList(lines.size() - (range.subLine() + 1), lines.size()).clear();
                i = range.row();
            } else {
                i = range.row() + 1;
            }
        }
        for (; i < content.rows(); i++) {
            var row = RowText.of(i, content.getText(i), fm);
            var subs = SubText.of(row, width);
            for (int j = 0; j < subs.size(); j++) {
                var sub = subs.get(j);
                lines.add(new SubRange(sub.row(), j, subs.size(), sub.fromIndex(), sub.toIndex()));
            }
        }
    }

    public void refreshRow(int startRow, int endRow) {
        int index = -1;
        var iterator = lines.iterator();
        for (int i = 0; i < lines.size(); i++) {
            var range = iterator.next();
            if (startRow <= range.row() && range.row() < endRow) {
                iterator.remove();
                if (index == -1) index = i;
            }
        }
        List<SubRange> newLines = IntStream.range(startRow, endRow).mapToObj(i -> {
            var row = RowText.of(i, content.getText(i), fm);
            var subs = SubText.of(row, width);
            List<SubRange> ret = new ArrayList<>();
            for (int j = 0; j < subs.size(); j++) {
                var sub = subs.get(j);
                ret.add(new SubRange(sub.row(), j, subs.size(), sub.fromIndex(), sub.toIndex()));
            }
            return ret;
        }).flatMap(Collection::stream).toList();

        lines.addAll(index, newLines);

    }

    public Text text(int line) {
        var range = lines.get(line);
        var row = RowText.of(range.row(), content.getText(range.row()), fm);
        var subs = SubText.of(row, width);
        return subs.get(range.subLine());
    }

    @Override
    public List<Text> texts(int startLine, int endLine) {
        if (startLine == endLine)  return List.of();
        if (startLine > endLine) {
            int tmp = startLine;
            startLine = endLine;
            endLine = tmp;
        }
        var startRange = lines.get(startLine);
        var endRange   = lines.get(endLine - 1);
        return IntStream.rangeClosed(startRange.row(), endRange.row()).mapToObj(i -> {
            var row = RowText.of(i, content.getText(i), fm);
            var subs = SubText.of(row, width);
            if (i == endRange.row() && subs.size() >= endRange.subLine + 1) {
                subs.subList(endRange.subLine + 1, subs.size()).clear();
            }
            if (i == startRange.row()) {
                subs.subList(0, startRange.subLine).clear();
            }
            return subs;
        }).flatMap(Collection::stream).map(Text.class::cast).toList();
    }

    public RowText rowText(int line) {
        var range = lines.get(line);
        return RowText.of(range.row(), content.getText(range.row()), fm);
    }

    @Override
    public double lineHeight() {
        return lineHeight;
    }

    @Override
    public int lineSize() {
        return lines.size();
    }

    @Override
    public Optional<Loc> loc(int row, int col, int startLine, int endLine) {
        for (int i = startLine; i < endLine; i++) {
            SubRange sub = lines.get(i);
            if (sub.contains(row, col)) {
                return Optional.of(new Loc(x(i, col), y(i)));
            }
        }
        return Optional.empty();
    }

    public record SubRange(int row, int subLine, int subLines, int fromIndex, int toIndex) {
        static SubRange empty = new SubRange(0, 0, 0, 0, 0);
        public int length() {
            return toIndex - fromIndex;
        }
        boolean contains(int row, int col) {
            return this.row == row && this.fromIndex <= col && col < this.toIndex;
        }
    }

}
