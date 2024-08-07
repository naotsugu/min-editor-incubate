package code.editor;

import code.editor.javafx.FontMetrics;
import code.editor.syntax.Syntax;
import com.mammb.code.piecetable.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ScreenText {

    int MARGIN_TOP = 5;
    int MARGIN_LEFT = 2;
    int TAB_SIZE = 4;

    void draw(Draw draw);
    void size(double width, double height);
    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int n);
    void moveCaretRight();
    void moveCaretLeft();
    void moveCaretDown();
    void moveCaretUp();
    void input(String text);
    void delete();
    void backSpace();


    static ScreenText of(Document doc, FontMetrics fm, Syntax syntax) {
        return new PlainScreenText(doc, fm, syntax);
    }

    static ScreenText wrapOf(Document doc, FontMetrics fm, Syntax syntax) {
        return new WrapScreenText(doc, fm, syntax);
    }

    /**
     * PlainScreenText.
     */
    class PlainScreenText implements ScreenText {
        double width = 0;
        double height = 0;
        final Document doc;
        final FontMetrics fm;
        Syntax syntax;
        List<TextRow> buffer = new ArrayList<>();
        List<Caret> carets = new ArrayList<>();
        int screenLineSize = 0;

        public PlainScreenText(Document doc, FontMetrics fm, Syntax syntax) {
            this.doc = doc;
            this.fm = fm;
            this.syntax = syntax;
            carets.add(new Caret());
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();
            double y = 0;
            for (TextRow row : buffer) {
                double x = 0;
                for (StyledText st : row.styledTexts()) {
                    draw.text(st.text, x + MARGIN_LEFT, y + MARGIN_TOP, st.styles);
                    x += st.width;
                }
                y += row.lineHeight;
            }
            for (Caret caret : carets) {
                if (!buffer.isEmpty() &&
                        buffer.getFirst().row <= caret.row &&
                        caret.row <= buffer.getLast().row) {
                    double x = colToX(caret.row, caret.col);
                    caret.vPos = (caret.vPos < 0) ? x : caret.vPos;
                    draw.caret(
                            Math.min(x, caret.vPos) + MARGIN_LEFT,
                            rowToY(caret.row) + MARGIN_TOP,
                            fm.getLineHeight());
                }
            }
        }

        @Override
        public void size(double width, double height) {
            if (width <= 0 || height <= 0) return;
            int newScreenLineSize = screenLineSize(height);
            if (this.height > height) {
                // shrink height
                int fromIndex = newScreenLineSize + 1;
                if (fromIndex < buffer.size() - 1) {
                    buffer.subList(fromIndex, buffer.size()).clear();
                }
            } else if (this.height < height) {
                // grow height
                int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
                for (int i = buffer.size(); i <= newScreenLineSize && i < doc.rows(); i++) {
                    buffer.add(createRow(top + i));
                }
            }
            this.width = width;
            this.height = height;
            this.screenLineSize = newScreenLineSize;
        }

        @Override
        public void scrollNext(int delta) {
            assert delta > 0;

            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int maxTop = (int) (doc.rows() - screenLineSize * 0.6);
            if (top + delta >= maxTop) {
                delta = maxTop - top;
            }

            int next = buffer.isEmpty() ? 0 : buffer.getLast().row + 1;
            buffer.subList(0, Math.min(delta, buffer.size())).clear();
            for (int i = next; i < (next + delta) && i < doc.rows(); i++) {
                buffer.add(createRow(i));
            }
        }

        @Override
        public void scrollPrev(int delta) {
            assert delta > 0;
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            delta = Math.clamp(delta, 0, top);
            if (delta == 0) return;
            if (buffer.size() >= screenLineSize) {
                buffer.subList(buffer.size() - delta, buffer.size()).clear();
            }
            for (int i = 1; i <= delta; i++) {
                buffer.addFirst(createRow(top - i));
            }
        }

        @Override
        public void scrollAt(int row) {
            row = Math.clamp(row, 0, doc.rows() - 1);
            buffer.clear();
            for (int i = row; i < doc.rows(); i++) {
                buffer.add(createRow(i));
                if (buffer.size() >= screenLineSize) break;
            }
        }

        @Override
        public void moveCaretRight() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                // TODO skip if eof
                TextRow row = textRowAt(caret.row);
                caret.col += row.isHighSurrogate(caret.col) ? 2 : 1;
                if (caret.col > row.textLength()) {
                    caret.col = 0;
                    caret.row = Math.min(caret.row + 1, doc.rows());
                }
            }
        }

        @Override
        public void moveCaretLeft() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                if (caret.isZero()) continue;
                TextRow textRow = textRowAt(caret.row);
                if (caret.col > 0) {
                    caret.col -= textRow.isLowSurrogate(caret.col - 1) ? 2 : 1;
                } else {
                    caret.row = Math.max(0, caret.row - 1);
                    caret.col = buffer.get(caret.row).textLength();
                }
            }
        }

        @Override
        public void moveCaretDown() {
            for (Caret caret : carets) {
                if (caret.row == doc.rows()) continue;
                caret.vPos = (caret.vPos < 0) ? colToX(caret.row, caret.col) : caret.vPos;
                caret.row++;
                caret.col = xToCol(caret.row, caret.vPos);
            }
        }

        @Override
        public void moveCaretUp() {
            for (Caret caret : carets) {
                if (caret.row == 0) continue;
                caret.vPos = (caret.vPos < 0) ? colToX(caret.row, caret.col) : caret.vPos;
                caret.row--;
                caret.col = xToCol(caret.row, caret.vPos);
            }
        }

        @Override
        public void input(String text) {
            for (Caret caret : carets) {
                caret.vPos = -1;
                doc.insert(caret.row, caret.col, text);
                int bufferIndex = bufferIndexOf(caret.row);
                if (bufferIndex >= 0) buffer.set(bufferIndex, createRow(caret.row));
                caret.col += text.length();
            }
        }

        @Override
        public void delete() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                // TODO end of line delete
                doc.delete(caret.row, caret.col,
                        Character.isHighSurrogate(doc.getText(caret.row).charAt(caret.col)) ? 2 : 1);
                int bufferIndex = bufferIndexOf(caret.row);
                if (bufferIndex >= 0) buffer.set(bufferIndex, createRow(caret.row));
            }
        }

        @Override
        public void backSpace() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                if (caret.isZero()) continue;
                if (caret.col == 0) {
                    moveCaretLeft();
                    delete();
                } else {
                    int len = Character.isLowSurrogate(doc.getText(caret.row).charAt(caret.col - 1)) ? 2 : 1;
                    caret.col -= len;
                    doc.delete(caret.row, caret.col, len);
                    int bufferIndex = bufferIndexOf(caret.row);
                    if (bufferIndex >= 0) buffer.set(bufferIndex, createRow(caret.row));
                }
            }
        }

        private int bufferIndexOf(int row) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int index = row - top;
            return (0 <= index && index < buffer.size()) ? index : -1;
        }

        private TextRow createRow(int i) {
            var row = new TextRow(i, doc.getText(i).toString(), fm);
            row.styles.putAll(syntax.apply(row.text));
            return row;
        }

        private double rowToY(int row) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            return (row - top) * fm.getLineHeight();
        }

        private double colToX(int row, int col) {
            float[] advances = textRowAt(row).advances;
            double x = 0;
            for (int i = 0; i < advances.length && i < col; i++) {
                x += advances[i];
            }
            return x;
        }

        private int xToCol(int row, double x) {
            if (x <= 0) return 0;
            TextRow textRow = textRowAt(row);
            float[] advances = textRow.advances;
            for (int i = 0; i < advances.length; i++) {
                x -= advances[i];
                if (x < 0) return i;
            }
            return textRow.textLength();
        }
        private int screenLineSize(double screenHeight) {
            return (int) Math.ceil(Math.max(0, screenHeight - MARGIN_TOP) / fm.getLineHeight());
        }

        private TextRow textRowAt(int row) {
            if (!buffer.isEmpty()) {
                return createRow(row);
            }
            var top = buffer.getFirst();
            if (top.row == row) return top;
            if (top.row <= row && row < top.row + buffer.size()) {
                return buffer.get(row - top.row);
            } else {
                return createRow(row);
            }
        }
    }

    /**
     * WrapScreenText
     */
    class WrapScreenText implements ScreenText {
        double width = 0;
        double height = 0;
        double wrap = 0;
        int topLine = 0;
        private final Document doc;
        private final FontMetrics fm;
        Syntax syntax;
        List<TextLine> buffer = new ArrayList<>();
        List<RowMap> wrapLayout = new ArrayList<>();
        List<Caret> carets = new ArrayList<>();
        int screenLineSize = 0;
        public WrapScreenText(Document doc, FontMetrics fm, Syntax syntax) {
            this.doc = doc;
            this.fm = fm;
            this.syntax = syntax;
            carets.add(new Caret());
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();
            double y = 0;
            for (TextLine line : buffer) {
                double x = 0;
                for (StyledText st : line.styledTexts()) {
                    draw.text(st.text, x + MARGIN_LEFT, y + MARGIN_TOP, st.styles);
                    x += st.width;
                }
                y += line.lineHeight();
            }
            for (Caret caret : carets) {
                for (int i = 0; i < buffer.size(); i++) {
                    TextLine textLine = buffer.get(i);
                    if (textLine.contains(caret.row, caret.col)) {
                        double cy = i * textLine.lineHeight();
                        double cx = 0;
                        for (int j = textLine.map.fromIndex; j < textLine.map.toIndex && j < caret.col; j++) {
                            cx += textLine.parent.advances[j];
                        }
                        draw.caret(cx + MARGIN_LEFT, cy + MARGIN_TOP, fm.getLineHeight());
                        break;
                    }
                }
            }
        }

        @Override
        public void size(double width, double height) {

            if (width <= 0 || height <= 0 ||
                    (this.width == width && this.height == height)) return;

            int newScreenLineSize = screenLineSize(height);

            if (this.width != width) {
                RowMap top = buffer.isEmpty() ? RowMap.empty : buffer.getFirst().map;
                this.wrap = width - MARGIN_LEFT - fm.getLineHeight() / 3;
                wrapLayout.clear();
                buffer.clear();
                for (int i = 0; i < doc.rows(); i++) {
                    for (TextLine line : createRow(i).wrap(wrap)) {
                        wrapLayout.add(line.map);
                        if (top.row <= line.map.row) {
                            if (top.row == line.map.row && top.subLine > line.map.subLine) continue;
                            if (buffer.size() < newScreenLineSize) {
                                buffer.add(line);
                            }
                        }
                    }
                }
            } else {
                if (this.height > height) {
                    int fromIndex = newScreenLineSize + 1;
                    if (fromIndex < buffer.size() - 1) {
                        buffer.subList(fromIndex, buffer.size()).clear();
                    }
                } else if (this.height < height) {
                    RowMap bottom = buffer.isEmpty() ? RowMap.empty : buffer.getLast().map;
                    for (int i = bottom.row; i < doc.rows(); i++) {
                        for (TextLine line : createRow(i).wrap(wrap)) {
                            if (bottom.row == line.map.row && bottom.subLine <= line.map.subLine) continue;
                            buffer.add(line);
                        }
                    }
                }
            }
            this.width = width;
            this.height = height;
            this.screenLineSize = newScreenLineSize;
        }

        @Override
        public void scrollNext(int delta) {
            int maxTop = (int) (wrapLayout.size() - screenLineSize * 0.6);
            if (topLine + delta >= maxTop) {
                delta = maxTop - topLine;
            }
            scrollAt(topLine + delta);
        }

        @Override
        public void scrollPrev(int delta) {
            scrollAt(topLine - delta);
        }

        @Override
        public void scrollAt(int line) {
            topLine = Math.clamp(line, 0, wrapLayout.size());
            buffer.clear();
            RowMap map = wrapLayout.get(topLine);
            for (int i = map.row; i < doc.rows(); i++) {
                List<TextLine> lines = createRow(i).wrap(wrap);
                int start = (i == map.row) ? map.subLine : 0;
                for (int j = start; j < lines.size(); j++) {
                    buffer.add(lines.get(j));
                    if (buffer.size() >= screenLineSize) break;
                }
            }
        }

        @Override
        public void moveCaretRight() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                var row = new TextRow(caret.row, doc.getText(caret.row).toString(), fm);
                caret.col += row.isHighSurrogate(caret.col) ? 2 : 1;
                if (caret.col > row.textLength()) {
                    caret.col = 0;
                    caret.row = Math.min(caret.row + 1, doc.rows());
                }
            }
        }

        @Override
        public void moveCaretLeft() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                if (caret.isZero()) continue;
                var row = new TextRow(caret.row, doc.getText(caret.row).toString(), fm);
                if (caret.col > 0) {
                    caret.col -= row.isLowSurrogate(caret.col - 1) ? 2 : 1;
                } else {
                    caret.row = Math.max(0, caret.row - 1);
                    caret.col = buffer.get(caret.row).textLength();
                }
            }
        }

        @Override
        public void moveCaretDown() {
            for (Caret caret : carets) {
                Loc loc = posToLoc(caret.row, caret.col);
                caret.vPos = (caret.vPos < 0) ? loc.x : caret.vPos;
                Pos pos = locToPos(caret.vPos,  loc.y + fm.getLineHeight());
                caret.row = pos.row;
                caret.col = pos.col;
            }
        }

        @Override
        public void moveCaretUp() {
            for (Caret caret : carets) {
                Loc loc = posToLoc(caret.row, caret.col);
                caret.vPos = (caret.vPos < 0) ? loc.x : caret.vPos;
                Pos pos = locToPos(caret.vPos,  loc.y - fm.getLineHeight());
                caret.row = pos.row;
                caret.col = pos.col;
            }
        }

        @Override
        public void input(String text) {
            for (Caret caret : carets) {
                caret.vPos = -1;
                doc.insert(caret.row, caret.col, text);
                refreshBuffer(caret.row, countLines(text));
                caret.col += text.length();
            }
        }

        @Override
        public void delete() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                delete(caret.row, caret.col,
                        Character.isHighSurrogate(doc.getText(caret.row).charAt(caret.col)) ? 2 : 1);
            }
        }

        private void delete(int row, int col, int len) {
            var text = doc.getText(row, col, len);
            int lines = countLines(text);
            doc.delete(row, col, len);
            refreshBuffer(row, lines);
        }

        @Override
        public void backSpace() {
            for (Caret caret : carets) {
                caret.vPos = -1;
                if (caret.isZero()) continue;
                moveCaretLeft();
                delete();
            }
        }

        private void refreshBuffer(int row, int rowLen) {
            List<TextLine> lines = new ArrayList<>();
            for (int i = row; i < row + rowLen; i++) {
                lines.addAll(createRow(i).wrap(wrap));
            }
            int[] bufferIndex = bufferIndexOf(row, rowLen);
            if (bufferIndex.length > 1) {
                buffer.subList(bufferIndex[0], bufferIndex[1]).clear();
                buffer.addAll(bufferIndex[0], lines);
                clampBuffer();
                wrapLayout.subList(topLine + bufferIndex[0], topLine + bufferIndex[1]).clear();
                wrapLayout.addAll(topLine + bufferIndex[0], lines.stream().map(l -> l.map).toList());
            } else {
                int[] wrapIndex = wrapLayoutIndexOf(row, rowLen);
                wrapLayout.subList(wrapIndex[0], wrapIndex[1]).clear();
                wrapLayout.addAll(wrapIndex[0], lines.stream().map(l -> l.map).toList());
            }
        }

        private int[] bufferIndexOf(int row, int len) {
            int from = -1, to = -1;
            for (int i = 0; i < buffer.size(); i++) {
                TextLine line = buffer.get(i);
                if (line.map.row == row && from < 0) {
                    from = i;
                } else if (line.map.row > (row + len - 1)) {
                    to = i;
                    break;
                }
            }
            if (to < 0) to = buffer.size();
            return (from < 0) ? new int[0] : new int[] { from, to };
        }

        private int[] wrapLayoutIndexOf(int row, int len) {
            int from = Collections.binarySearch(wrapLayout, new RowMap(row, 0, 0, 0), Comparator.comparing(RowMap::row));
            if (from < 0) return new int[0];
            int to = -1;
            for (int i = from; i < wrapLayout.size(); i++) {
                RowMap map = wrapLayout.get(i);
                if (map.row > (row + len - 1)) {
                    to = i;
                    break;
                }
            }
            if (to < 0) to = wrapLayout.size();
            return new int[] { from, to };
        }

        private int screenLineSize(double h) {
            return (int) Math.ceil(Math.max(0, h - MARGIN_TOP) / fm.getLineHeight());
        }

        private TextRow createRow(int row) {
            var textRow = new TextRow(row, doc.getText(row).toString(), fm);
            textRow.styles.putAll(syntax.apply(textRow.text));
            return textRow;
        }

        private void clampBuffer() {
            int screenLineSize = screenLineSize(height);
            if (buffer.size() > screenLineSize) {
                buffer.subList(screenLineSize, buffer.size()).clear();
            }
        }

        private Loc posToLoc(int row, int col) {
            Indexed<TextLine> line = posToLine(row, col);
            double y = (line.index - topLine) * fm.getLineHeight();
            double x = 0;
            TextLine textLine = line.value();
            for (int j = textLine.map.fromIndex; j < textLine.map.toIndex && j < col; j++) {
                x += textLine.parent.advances[j];
            }
            return new Loc(x, y);
        }

        private Indexed<TextLine> posToLine(int row, int col) {
            // calc loc from screen buffer
            for (int i = 0; i < buffer.size(); i++) {
                TextLine textLine = buffer.get(i);
                if (textLine.contains(row, col)) {
                    return new Indexed<>(topLine + i, textLine);
                }
            }
            // calc loc from wrapLayout
            for (int i = 0; i < wrapLayout.size(); i++) {
                RowMap map = wrapLayout.get(i);
                if (map.contains(row, col)) {
                    return new Indexed<>(i, new TextRow(map.row, doc.getText(map.row).toString(), fm).wrap(wrap).get(map.subLine));
                }
            }
            return new Indexed<>(wrapLayout.size(),
                    new TextRow(doc.rows(), doc.getText(doc.rows()).toString(), fm).wrap(wrap).getLast());
        }

        private Pos locToPos(double x, double y) {
            RowMap map = wrapLayout.get(topLine + (int) (y / fm.getLineHeight()));
            TextLine textLine = posToLine(map.row, map.fromIndex).value();
            int col = textLine.map.fromIndex;
            for (int i = 0; i < textLine.textLength(); i++) {
                if (x <= 0) break;
                x -= textLine.parent.advances[col++];
            }
            return new Pos(map.row, col);
        }

    }

    record Loc(double x, double y) { }
    record Pos(int row, int col) { }
    record RowMap(int row, int subLine, int fromIndex, int toIndex) {
        static RowMap empty = new RowMap(0, 0, 0, 0);
        int length() { return toIndex - fromIndex; }
        boolean contains(int row, int col) {
            return this.row == row && this.fromIndex <= col && col < this.toIndex;
        }
    }
    record Indexed<E>(int index, E value) { }

    class TextRow {
        int row;
        String text;
        float[] advances;
        Styles styles;
        float lineHeight;
        public TextRow(int row, String text, FontMetrics fm) {
            this.row = row;
            this.text = text;
            this.advances = advances(text, fm);
            this.styles = new Styles();
            this.lineHeight = fm.getLineHeight();
        }
        private List<TextLine> wrap(double width) {
            if (width <= 0) {
                return List.of(new TextLine(this,
                        new RowMap(row, 0, 0, text.length())));
            }
            double w = 0;
            int fromIndex = 0;
            List<TextLine> wrapped = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                float advance = advances[i];
                if (advance <= 0) continue;
                if (w + advance > width) {
                    wrapped.add(new TextLine(this,
                            new RowMap(row, wrapped.size(), fromIndex, i)));
                    w = 0;
                    fromIndex = i;
                }
                w += advance;
            }
            wrapped.add(new TextLine(this,
                    new RowMap(row, wrapped.size(), fromIndex, text.length())));
            return wrapped;
        }

        List<StyledText> styledTexts() {
            return styles.apply(text, advances);
        }
        boolean isSurrogate(int index) { return Character.isSurrogate(text.charAt(index)); }
        boolean isHighSurrogate(int index) { return Character.isHighSurrogate(text.charAt(index)); }
        boolean isLowSurrogate(int index) { return Character.isLowSurrogate(text.charAt(index)); }
        int length() { return text.length(); }
        int textLength() {
            if (text.length() >= 2 && text.charAt(text.length() - 2) == '\r' && text.charAt(text.length() - 1) == '\n') {
                return text.length() - 2;
            }
            if (!text.isEmpty() && text.charAt(text.length() - 1) == '\n') {
                return text.length() - 1;
            }
            return text.length();
        }
    }

    class TextLine {
        TextRow parent;
        RowMap map;
        public TextLine(TextRow parent, RowMap map) {
            this.parent = parent;
            this.map = map;
        }
        int row() { return map.row; }
        int subLine() { return map.subLine; }
        float lineHeight() { return parent.lineHeight; }
        String text() { return parent.text.substring(map.fromIndex, map.toIndex); }
        List<StyledText> styledTexts() { return parent.styles.apply(map.fromIndex, map.toIndex, parent.text, parent.advances); }
        boolean isSurrogate(int index) { return parent.isSurrogate(map.fromIndex + index); }
        boolean isHighSurrogate(int index) { return parent.isHighSurrogate(map.fromIndex + index); }
        boolean isLowSurrogate(int index) { return parent.isLowSurrogate(map.fromIndex + index); }
        int length() { return map.length(); }
        boolean contains(int row, int col) { return map.contains(row, col); }
        int textLength() {
            if (map.length() >= 2 && parent.text.charAt(map.toIndex - 2) == '\r' && parent.text.charAt(map.toIndex - 1) == '\n') {
                return map.length() - 2;
            }
            if (map.length() >= 1 && parent.text.charAt(map.toIndex - 1) == '\n') {
                return map.length() - 1;
            }
            return map.length();
        }
    }

    private static float[] advances(String text, FontMetrics fm) {
        float[] advances = new float[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char ch1 = text.charAt(i);
            if (Character.isHighSurrogate(ch1)) {
                advances[i] = fm.getAdvance(ch1, text.charAt(i + 1));
                i++;
            } else if (Character.isISOControl(ch1)) {
                i++;
            } else if (ch1 == '\t') {
                advances[i] = fm.getAdvance(" ".repeat(TAB_SIZE));
            } else {
                advances[i] = fm.getAdvance(ch1, (char) 0);
            }
        }
        return advances;
    }

    private static float width(float[] advances, int from, int to) {
        float ret = 0;
        for (int i = from; i < to; i++) ret += advances[i];
        return ret;
    }

    private static int countLines(CharSequence text) {
        return 1 + (int) text.codePoints().filter(c -> c == '\n').count();
    }

    class Caret {
        int row = 0, col = 0;
        double vPos = 0; // not contains margin
        public boolean isZero() { return row == 0 && col == 0; }
    }

    sealed interface Style {}
    record TextColor(String colorString) implements Style {}
    record BgColor(String colorString) implements Style {}
    record Selected() implements Style {}
    record Emphasize() implements Style {}

    record StyleSpan(Style style, int offset, int length) { }
    record StyledText(String text, float width, List<Style> styles) { }
    class Styles {
        private final Set<Integer> bounds = new HashSet<>();
        private final List<StyleSpan> spans = new ArrayList<>();
        void putAll(List<StyleSpan> spans) {
            spans.forEach(this::put);
        }
        void put(StyleSpan span) {
            bounds.add(span.offset);
            bounds.add(span.offset + span.length);
            spans.add(span);
        }
        List<StyledText> apply(String text, float[] advances) {
            return apply(0, text.length(), text, advances);
        }
        List<StyledText> apply(int from, int to, String text, float[] advances) {
            assert text.length() == advances.length;

            List<Integer> list = bounds.stream()
                    .filter(i -> from <= i && i <= to)
                    .sorted()
                    .collect(Collectors.toList());

            if (list.isEmpty()) {
                return List.of(new StyledText(
                        text.substring(from, to),
                        width(advances, from, to),
                        List.of()));
            }

            if (list.getFirst() != from) {
                list.addFirst(from);
            }
            if (list.getLast() != to) {
                list.addLast(to);
            }
            List<StyledText> ret = new ArrayList<>();
            for (int i = 0; i < list.size() - 1; i++) {
                int start = list.get(i);
                int end   = list.get(i + 1);
                ret.add(new StyledText(
                        text.substring(start, end),
                        width(advances, start, end),
                        stylesOf(list.get(i))));
            }
            return ret;

        }
        private List<Style> stylesOf(int index) {
            return spans.stream()
                    .filter(span -> span.offset <= index && index < (span.offset + span.length))
                    .map(span -> span.style)
                    .toList();
        }
    }

}
