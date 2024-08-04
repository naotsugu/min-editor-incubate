package code.editor;

import code.editor.javafx.FontMetrics;
import code.editor.syntax.Syntax;
import com.mammb.code.piecetable.Document;
import java.util.ArrayList;
import java.util.Arrays;
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
            double y = MARGIN_TOP;
            for (TextRow row : buffer) {
                double x = MARGIN_LEFT;
                for (StyledText st : row.styledTexts()) {
                    draw.text(st.text, x, y, st.styles);
                    x += st.width;
                }
                y += row.lineHeight;
            }
            for (Caret caret : carets) {
                if (!buffer.isEmpty() &&
                        buffer.getFirst().row <= caret.row &&
                        caret.row <= buffer.getLast().row) {
                    draw.caret(colToX(caret.row, caret.col), rowToY(caret.row), fm.getLineHeight());
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

        private TextRow createRow(int i) {
            var row = new TextRow(i, doc.getText(i).toString(), fm);
            row.styles.putAll(syntax.apply(row.text));
            return row;
        }

        private int yToRow(double y) {
            y = Math.max(0, y - MARGIN_TOP);
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            return Math.min(doc.rows(), top + screenLineSize(y)) + MARGIN_TOP;
        }

        private double rowToY(int row) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            return (row - top) * fm.getLineHeight() + MARGIN_TOP;
        }

        private double colToX(int row, int col) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int line = (row - top);
            if (0 <= line && line < buffer.size()) {
                float[] advances = buffer.get(line).advances;
                double x = MARGIN_LEFT;
                for (int i = 0; i < advances.length && i < col; i++) {
                    x += advances[i];
                }
                return x;
            }
            return MARGIN_LEFT;
        }

        private double xToCol(int row, double x) {
            if (x <= MARGIN_LEFT) return MARGIN_LEFT;
            x = Math.max(0, x - MARGIN_LEFT);
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row;
            int line = (row - top);
            if (0 <= line && line < buffer.size()) {
                float[] advances = buffer.get(line).advances;
                for (int i = 0; i < advances.length; i++) {
                    x -= advances[i];
                    if (x <= 0) {
                        return i;
                    }
                }
                return advances.length;
            }
            return 0;
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
        List<TextMap> wrapLayout = new ArrayList<>();
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
            double y = MARGIN_TOP;
            for (TextLine line : buffer) {
                double x = MARGIN_LEFT;
                for (StyledText st : line.styledTexts()) {
                    draw.text(st.text, x, y, st.styles);
                    x += st.width;
                }
                y += line.lineHeight();
            }
            for (Caret caret : carets) {
                for (int i = 0; i < buffer.size(); i++) {
                    TextLine textLine = buffer.get(i);
                    if (textLine.row() == caret.row && textLine.map.fromIndex <= caret.col && caret.col < textLine.map.toIndex) {
                        double cy = i * textLine.lineHeight() + MARGIN_TOP;
                        double cx = MARGIN_LEFT;
                        for (int j = textLine.map.fromIndex; i < textLine.map.toIndex && j < caret.col; j++) {
                            cx += textLine.parent.advances[j];
                        }
                        draw.caret(cx, cy, fm.getLineHeight());
                        break;
                    }
                }
            }
        }

        @Override
        public void size(double width, double height) {

            if (width <= 0 || height <=0 ||
                    (this.width == width && this.height == height)) return;

            int newScreenLineSize = screenLineSize(height);

            if (this.width != width) {
                TextMap top = buffer.isEmpty() ? TextMap.empty : buffer.getFirst().map;
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
                    TextMap bottom = buffer.isEmpty() ? TextMap.empty : buffer.getLast().map;
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
            topLine = Math.clamp(line, 0, wrapLayout.size() - 1);
            buffer.clear();
            TextMap map = wrapLayout.get(topLine);
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

        private int screenLineSize(double h) {
            return (int) Math.ceil(Math.max(0, h - MARGIN_TOP) / fm.getLineHeight());
        }

        private TextRow createRow(int i) {
            var row = new TextRow(i, doc.getText(i).toString(), fm);
            row.styles.putAll(syntax.apply(row.text));
            return row;
        }

    }

    record TextMap(int row, int subLine, int fromIndex, int toIndex) {
        static TextMap empty = new TextMap(0, 0, 0, 0);
        int length() { return toIndex - fromIndex; }
    }

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
                        new TextMap(row, 0, 0, text.length())));
            }
            double w = 0;
            int fromIndex = 0;
            List<TextLine> wrapped = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                float advance = advances[i];
                if (advance <= 0) continue;
                if (w + advance > width) {
                    wrapped.add(new TextLine(this,
                            new TextMap(row, wrapped.size(), fromIndex, i)));
                    w = 0;
                    fromIndex = i;
                }
                w += advance;
            }
            wrapped.add(new TextLine(this,
                    new TextMap(row, wrapped.size(), fromIndex, text.length())));
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
        TextMap map;
        public TextLine(TextRow parent, TextMap map) {
            this.parent = parent;
            this.map = map;
        }
        int row() { return map.row; }
        int subLine() { return map.subLine; }
        float lineHeight() { return parent.lineHeight; }
        String text() { return parent.text.substring(map.fromIndex, map.toIndex); }
        boolean hasNextLine() { return map.toIndex < parent.text.length(); }
        boolean hasPrevLine() { return map.fromIndex > 0; }
        List<StyledText> styledTexts() { return parent.styles.apply(map.fromIndex, map.toIndex, parent.text, parent.advances); }
        boolean isSurrogate(int index) { return parent.isSurrogate(map.fromIndex + index); }
        boolean isHighSurrogate(int index) { return parent.isHighSurrogate(map.fromIndex + index); }
        boolean isLowSurrogate(int index) { return parent.isLowSurrogate(map.fromIndex + index); }
        int length() { return map.length(); }
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

    class Caret {
        int row, col;
        public Caret(int row, int col) { this.row = row; this.col = col; }
        public Caret() { this(0, 0); }
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
